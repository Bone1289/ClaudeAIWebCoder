package com.example.demo.adapter.in.web.banking.dto;

import com.example.demo.domain.AccountStatement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class AccountStatementResponse {
    private AccountResponse account;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal netChange;
    private int transactionCount;
    private List<TransactionResponse> transactions;

    public static AccountStatementResponse fromDomain(AccountStatement statement) {
        AccountStatementResponse response = new AccountStatementResponse();
        response.account = AccountResponse.fromDomain(statement.account());
        response.startDate = statement.startDate();
        response.endDate = statement.endDate();
        response.openingBalance = statement.openingBalance();
        response.closingBalance = statement.closingBalance();
        response.totalDeposits = statement.totalDeposits();
        response.totalWithdrawals = statement.totalWithdrawals();
        response.netChange = statement.netChange();
        response.transactionCount = statement.transactionCount();
        response.transactions = statement.transactions().stream()
                .map(TransactionResponse::fromDomain)
                .collect(Collectors.toList());
        return response;
    }

    // Getters and Setters
    public AccountResponse getAccount() { return account; }
    public void setAccount(AccountResponse account) { this.account = account; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }
    public BigDecimal getClosingBalance() { return closingBalance; }
    public void setClosingBalance(BigDecimal closingBalance) { this.closingBalance = closingBalance; }
    public BigDecimal getTotalDeposits() { return totalDeposits; }
    public void setTotalDeposits(BigDecimal totalDeposits) { this.totalDeposits = totalDeposits; }
    public BigDecimal getTotalWithdrawals() { return totalWithdrawals; }
    public void setTotalWithdrawals(BigDecimal totalWithdrawals) { this.totalWithdrawals = totalWithdrawals; }
    public BigDecimal getNetChange() { return netChange; }
    public void setNetChange(BigDecimal netChange) { this.netChange = netChange; }
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    public List<TransactionResponse> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionResponse> transactions) { this.transactions = transactions; }
}
