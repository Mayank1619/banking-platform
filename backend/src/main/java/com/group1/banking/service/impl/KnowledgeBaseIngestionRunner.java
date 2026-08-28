package com.group1.banking.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.group1.banking.config.ChatbotAiConfig;

/**
 * Seeds the pgvector knowledge base from the curated savings articles under
 * {@code resources/knowledge-base/} on startup. Idempotent: skips ingestion if the
 * vector table already has content, so restarts don't duplicate entries.
 */
@Component
public class KnowledgeBaseIngestionRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseIngestionRunner.class);
    private static final String KNOWLEDGE_BASE_LOCATION_PATTERN = "classpath:knowledge-base/*.md";

    private final VectorStore vectorStore;
    private final JdbcTemplate chatbotVectorJdbcTemplate;

    public KnowledgeBaseIngestionRunner(VectorStore vectorStore,
                                         @Qualifier("chatbotVectorJdbcTemplate") JdbcTemplate chatbotVectorJdbcTemplate) {
        this.vectorStore = vectorStore;
        this.chatbotVectorJdbcTemplate = chatbotVectorJdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (alreadyIngested()) {
                log.info("Savings knowledge base already ingested; skipping seed on startup.");
                return;
            }

            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(KNOWLEDGE_BASE_LOCATION_PATTERN);

            if (resources.length == 0) {
                log.warn("No knowledge base articles found at {}", KNOWLEDGE_BASE_LOCATION_PATTERN);
                return;
            }

            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> chunks = new ArrayList<>();

            for (Resource resource : resources) {
                TextReader reader = new TextReader(resource);
                reader.getCustomMetadata().put("source", resource.getFilename());
                reader.getCustomMetadata().put("type", "savings-knowledge-base");
                List<Document> docs = reader.get();
                chunks.addAll(splitter.apply(docs));
            }

            vectorStore.add(chunks);
            log.info("Ingested {} chunks from {} savings knowledge base articles.",
                    chunks.size(), resources.length);
        } catch (Exception ex) {
            // Non-fatal: the chatbot degrades to structured-data-only / fallback
            // responses if the knowledge base couldn't be seeded, rather than
            // failing application startup.
            log.error("Failed to seed savings knowledge base; chatbot will run without it until this is resolved.", ex);
        }
    }

    private boolean alreadyIngested() {
        Integer count = chatbotVectorJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + ChatbotAiConfig.KNOWLEDGE_BASE_TABLE, Integer.class);
        return count != null && count > 0;
    }
}
