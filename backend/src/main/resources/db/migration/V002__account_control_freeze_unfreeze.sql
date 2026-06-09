CREATE TABLE IF NOT EXISTS account_control_audit (
    event_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    admin_user_id VARCHAR(80) NOT NULL,
    admin_role VARCHAR(50) NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    previous_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    notes VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_aca_account FOREIGN KEY (account_id) REFERENCES account(account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_aca_account ON account_control_audit(account_id);
CREATE INDEX idx_aca_created_at ON account_control_audit(created_at);
