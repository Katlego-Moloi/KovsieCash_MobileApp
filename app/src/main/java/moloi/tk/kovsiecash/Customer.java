package moloi.tk.kovsiecash;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private String Username;
    private String Email;
    private int UserId;
    private ArrayList<Account> Accounts;

    // Constructor
    public Customer(int userId, String userName, String email, ArrayList<Account> accounts) {

        this.UserId = userId;
        this.Username = userName;
        this.Email = email;
        this.Accounts = accounts;
    }

    // Getters and Setters
    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        this.Username = username;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        this.Email = email;
    }

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        this.UserId = userId;
    }

    public ArrayList<Account> getAccounts() {
        return Accounts;
    }

    public void setAccount(ArrayList<Account> accounts) {
        this.Accounts = accounts;
    }
}