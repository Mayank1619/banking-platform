-- Savings Goal Tracker Feature
-- Table: savings_goals
-- Stores user savings goals with one active goal per account per customer
-- Soft delete pattern: deleted_at IS NULL indicates active record

CREATE TABLE IF NOT EXISTS savings_goals (
    goal_id             BIGINT          PRIMARY KEY AUTO_INCREMENT,
    customer_id         BIGINT          NOT NULL,
    account_id          BIGINT          NOT NULL,
    
    -- Q1: What are you saving for?
    -- Stores preset value OR custom free-text entry
    goal_name           VARCHAR(255)    NOT NULL,
    
    -- Q2: How much do you plan to save?
    target_amount       DECIMAL(19,2)   NOT NULL,
    
    -- Q3: By when?
    target_date         DATE            NOT NULL,
    
    -- Status (recalculated on every read)
    -- NOT_STARTED: balance=0 AND target_date >= today
    -- IN_PROGRESS: balance>0 AND <target AND target_date >= today
    -- ACHIEVED: balance >= target_amount
    -- OVERDUE: target_date < today AND balance < target_amount
    status              ENUM(
                          'NOT_STARTED',
                          'IN_PROGRESS',
                          'ACHIEVED',
                          'OVERDUE'
                        )               NOT NULL DEFAULT 'NOT_STARTED',
    
    -- Soft delete pattern (consistent with account, customers tables)
    deleted_at          TIMESTAMP       NULL,
    
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,
    
    -- One active goal per account per customer
    CONSTRAINT uq_sg_customer_account UNIQUE (customer_id, account_id),
    
    -- Foreign key constraints
    CONSTRAINT fk_sg_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_sg_account
        FOREIGN KEY (account_id) REFERENCES account(account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for query performance
CREATE INDEX idx_sg_customer_id ON savings_goals (customer_id);
CREATE INDEX idx_sg_account_id ON savings_goals (account_id);
CREATE INDEX idx_sg_status ON savings_goals (status);
