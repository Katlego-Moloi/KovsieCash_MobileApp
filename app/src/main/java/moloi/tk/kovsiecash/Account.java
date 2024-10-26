package moloi.tk.kovsiecash;

import java.util.List;

public class Account {
    private String accountNumber;
    private String accountName;
    private double balance;
    private int userId;
    private List<Transaction> transactions;

    // Constructor
    public Account(String accountNumber, String accountName, double balance, int userId, List<Transaction> transactions) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.balance = balance;
        this.userId = userId;
        this.transactions = transactions;
    }

    // Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}