package moloi.tk.kovsiecash;

import java.util.ArrayList;

public interface ITransactionRepository {
    public ArrayList<Transaction> getTransactionsRecent(int userId, int transactionCount);
}
