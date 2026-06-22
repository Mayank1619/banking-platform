package com.group1.banking.service;

import com.group1.banking.entity.AuditLogEntity;
import com.group1.banking.entity.AccountStatus;
import com.group1.banking.entity.accountcontrol.AccountControlActionType;
import com.group1.banking.entity.accountcontrol.AccountControlAuditEvent;
import com.group1.banking.repository.AccountControlAuditRepository;
import com.group1.banking.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuditService.
 */
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Mock
    private AccountControlAuditRepository accountControlAuditRepository;

    private AccountControlAuditService accountControlAuditService;

    @BeforeEach
    void initAccountControlAuditService() {
        accountControlAuditService = new AccountControlAuditService(accountControlAuditRepository);
    }

    @Test
    void log_shouldSaveAuditRecord_withCorrectFields() {
        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        when(auditLogRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        auditService.log("user-123", "ADMIN", "LOGIN", "USER", "user-123", "SUCCESS");

        AuditLogEntity saved = captor.getValue();
        assertThat(saved.getActorId()).isEqualTo("user-123");
        assertThat(saved.getActorRole()).isEqualTo("ADMIN");
        assertThat(saved.getAction()).isEqualTo("LOGIN");
        assertThat(saved.getResourceType()).isEqualTo("USER");
        assertThat(saved.getResourceId()).isEqualTo("user-123");
        assertThat(saved.getOutcome()).isEqualTo("SUCCESS");
    }

    @Test
    void log_shouldPersistToRepository() {
        when(auditLogRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        auditService.log("-1", "SYSTEM", "NOTIFICATION_FAILED", "NOTIFICATION", "evt-001", "ERROR");

        verify(auditLogRepository).save(org.mockito.ArgumentMatchers.any(AuditLogEntity.class));
    }

    @Test
    void log_shouldAcceptNullValues_withoutException() {
        when(auditLogRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        auditService.log(null, null, "ACTION", "TYPE", null, null);

        verify(auditLogRepository).save(org.mockito.ArgumentMatchers.any(AuditLogEntity.class));
    }

    @Test
    void accountControlLog_shouldSaveFreezeEvent_withRequiredFields() {
        ArgumentCaptor<AccountControlAuditEvent> captor = ArgumentCaptor.forClass(AccountControlAuditEvent.class);
        when(accountControlAuditRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        accountControlAuditService.logEvent(
                1001L,
                "admin-1",
                "ADMIN",
                AccountControlActionType.FREEZE,
                AccountStatus.ACTIVE,
                AccountStatus.FROZEN,
                "Fraud review",
                "queue item");

        AccountControlAuditEvent saved = captor.getValue();
        assertThat(saved.getAccountId()).isEqualTo(1001L);
        assertThat(saved.getActionType()).isEqualTo(AccountControlActionType.FREEZE);
        assertThat(saved.getPreviousStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(saved.getNewStatus()).isEqualTo(AccountStatus.FROZEN);
        assertThat(saved.getReason()).isEqualTo("Fraud review");
    }

    @Test
    void accountControlLog_shouldSaveUnfreezeEvent_withRequiredFields() {
        ArgumentCaptor<AccountControlAuditEvent> captor = ArgumentCaptor.forClass(AccountControlAuditEvent.class);
        when(accountControlAuditRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        accountControlAuditService.logEvent(
                1001L,
                "admin-1",
                "ADMIN",
                AccountControlActionType.UNFREEZE,
                AccountStatus.FROZEN,
                AccountStatus.ACTIVE,
                "Case resolved",
                "notes");

        AccountControlAuditEvent saved = captor.getValue();
        assertThat(saved.getActionType()).isEqualTo(AccountControlActionType.UNFREEZE);
        assertThat(saved.getPreviousStatus()).isEqualTo(AccountStatus.FROZEN);
        assertThat(saved.getNewStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(saved.getReason()).isEqualTo("Case resolved");
    }
}
