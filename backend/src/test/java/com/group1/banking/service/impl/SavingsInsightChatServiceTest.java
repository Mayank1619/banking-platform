package com.group1.banking.service.impl;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import com.group1.banking.dto.chat.ChatQueryResponse;
import com.group1.banking.repository.ChatInteractionLogRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the chatbot turn orchestration (T-BRD 6.1).
 *
 * These cover the acceptance criteria that live in this class rather than in the model:
 * that a personalized answer is distinguished from a limited-data fallback, that blocked
 * topics never reach the model, that a model failure still returns a controlled response
 * instead of an error, and that each of those is logged with the right traceability flags.
 *
 * The {@link ChatClient} is mocked, so no Groq call is made and sampling temperature is
 * irrelevant here. Tool calls are simulated by having the stubbed response record
 * citations on a real {@link SavingsChatCitationTracker}, which is exactly what the real
 * tools do from inside the model's tool-calling loop.
 */
class SavingsInsightChatServiceTest {

    private static final Long CUSTOMER_ID = 42L;

    private SavingsChatGuardrailService guardrailService;
    private ChatClient chatClient;
    private ChatInteractionLogRepository chatLogRepository;
    private SavingsChatCitationTracker citationTracker;
    private SavingsInsightChatService service;

    @BeforeEach
    void setUp() {
        guardrailService = new SavingsChatGuardrailService();
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        chatLogRepository = mock(ChatInteractionLogRepository.class);
        citationTracker = new SavingsChatCitationTracker();
        service = new SavingsInsightChatService(
                guardrailService, chatClient, chatLogRepository, citationTracker);
    }

    /**
     * Stubs the fluent ChatClient chain, running {@code duringCall} at the point the model
     * would be invoked so tests can simulate whichever tools the agent decided to call.
     */
    private void stubChatReply(String reply, Runnable duringCall) {
        when(chatClient.prompt()
                .user(anyString())
                .toolContext(any())
                .advisors(any(Consumer.class))
                .call()
                .content())
                .thenAnswer(invocation -> {
                    duringCall.run();
                    return reply;
                });
    }

    private void stubChatFailure(RuntimeException failure) {
        when(chatClient.prompt()
                .user(anyString())
                .toolContext(any())
                .advisors(any(Consumer.class))
                .call()
                .content())
                .thenThrow(failure);
    }

    @Test
    @DisplayName("Scenario 1: personal data retrieved -> personalized answer, logged as ANSWERED")
    void ask_whenPersonalDataUsed_returnsPersonalizedAnswer() {
        stubChatReply("You spent $312 on Food & Drink over the last 30 days.", () -> {
            citationTracker.recordCitation("Your recent transaction history");
            citationTracker.markPersonalDataUsed();
        });

        ChatQueryResponse response = service.ask(CUSTOMER_ID, "where is my money going?");

        assertThat(response.getResponse()).contains("$312");
        assertThat(response.isLimitedData()).isFalse();
        assertThat(response.isBlocked()).isFalse();
        assertThat(response.getBasedOn()).containsExactly("Your recent transaction history");

        verify(chatLogRepository).log(eq(CUSTOMER_ID), eq("where is my money going?"),
                anyString(), eq("ANSWERED"),
                eq(true), eq(false), eq(List.of("Your recent transaction history")));
    }

    @Test
    @DisplayName("Scenario 2: knowledge base only -> retrieval happened but answer is still a fallback")
    void ask_whenOnlyKnowledgeBaseUsed_flagsLimitedDataButRecordsRetrieval() {
        // The knowledge base tool records a citation but never marks personal data used,
        // which is the case the single `outcome` column used to hide.
        stubChatReply("An emergency fund usually covers three to six months of expenses.",
                () -> citationTracker.recordCitation("Savings knowledge base: emergency-funds.md"));

        ChatQueryResponse response = service.ask(CUSTOMER_ID, "what is an emergency fund?");

        assertThat(response.isLimitedData()).isTrue();
        assertThat(response.isBlocked()).isFalse();
        assertThat(response.getBasedOn()).containsExactly("Savings knowledge base: emergency-funds.md");

        verify(chatLogRepository).log(eq(CUSTOMER_ID), anyString(), anyString(), eq("FALLBACK"),
                eq(true), eq(true),
                eq(List.of("Savings knowledge base: emergency-funds.md")));
    }

    @Test
    @DisplayName("Scenario 2: no data retrieved at all -> limited data, no sources logged")
    void ask_whenNothingRetrieved_flagsLimitedDataAndNoRetrieval() {
        stubChatReply("Hello! I can help with savings and budgeting questions.", () -> { });

        ChatQueryResponse response = service.ask(CUSTOMER_ID, "hello");

        assertThat(response.isLimitedData()).isTrue();
        assertThat(response.getBasedOn()).isEmpty();

        verify(chatLogRepository).log(eq(CUSTOMER_ID), anyString(), anyString(), eq("FALLBACK"),
                eq(false), eq(true), eq(List.of()));
    }

    @Test
    @DisplayName("Scenario 5: blocked topic never reaches the model and is logged as GUARDRAIL_BLOCKED")
    void ask_whenTopicBlocked_declinesWithoutCallingModel() {
        ChatQueryResponse response = service.ask(CUSTOMER_ID, "which stock should I buy?");

        assertThat(response.isBlocked()).isTrue();
        assertThat(response.isLimitedData()).isFalse();
        assertThat(response.getBasedOn()).isEmpty();
        assertThat(response.getResponse()).contains("not able to advise on that topic");

        verify(chatClient, never()).prompt();
        verify(chatLogRepository).log(eq(CUSTOMER_ID), eq("which stock should I buy?"),
                anyString(), eq("GUARDRAIL_BLOCKED"),
                eq(false), eq(false), eq(List.of()));
    }

    @Test
    @DisplayName("Scenario 3: model failure returns the controlled fallback, not an error")
    void ask_whenChatClientFails_returnsFallbackResponse() {
        stubChatFailure(new IllegalStateException("groq unavailable"));

        ChatQueryResponse response = service.ask(CUSTOMER_ID, "how are my savings goals doing?");

        assertThat(response.getResponse()).isEqualTo(
                "I'm unable to generate a response right now. Please try again in a moment.");
        assertThat(response.isLimitedData()).isTrue();
        assertThat(response.isBlocked()).isFalse();
        assertThat(response.getBasedOn()).isEmpty();

        verify(chatLogRepository).log(eq(CUSTOMER_ID), anyString(), anyString(), eq("ERROR"),
                eq(false), eq(true), eq(List.of()));
    }

    @Test
    @DisplayName("A blank message is refused before the model is called")
    void ask_whenMessageBlank_isBlocked() {
        ChatQueryResponse response = service.ask(CUSTOMER_ID, "   ");

        assertThat(response.isBlocked()).isTrue();
        verify(chatClient, never()).prompt();
        verify(chatLogRepository).log(anyLong(), anyString(), anyString(),
                eq("GUARDRAIL_BLOCKED"), anyBoolean(), anyBoolean(), anyList());
    }

    @Test
    @DisplayName("Citation state does not leak from one turn into the next on the same thread")
    void ask_doesNotLeakCitationsBetweenTurns() {
        stubChatReply("Your Emergency Fund goal is 50% complete.", () -> {
            citationTracker.recordCitation("Your savings goal progress");
            citationTracker.markPersonalDataUsed();
        });
        service.ask(CUSTOMER_ID, "how is my goal going?");

        // Second turn retrieves nothing; it must not inherit the first turn's citations.
        stubChatReply("Happy to help with savings questions.", () -> { });
        ChatQueryResponse second = service.ask(CUSTOMER_ID, "thanks");

        assertThat(second.getBasedOn()).isEmpty();
        assertThat(second.isLimitedData()).isTrue();
    }
}
