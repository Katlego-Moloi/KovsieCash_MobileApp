package moloi.tk.kovsiecash;

import java.util.List;

public interface IAccountRepository {

    public void insertAccount(String accountName, String accountNumber, double balance, long userId);

    public List<String> getAccountNamesByUser(int userId);

    public List<Account> getAccountsByUser(int userId, int transactionCount);
}


