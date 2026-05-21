package com.group1.banking.dto.customer;

import java.math.BigDecimal;

import com.group1.banking.entity.AccountType;

import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
        @NotNull AccountType accountType,
        BigDecimal balance,
        BigDecimal interestRate) {
}
