package moloi.tk.kovsiecash;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.ColorSpace;

import java.util.ArrayList;
import java.util.List;

public class AccountRepository implements IAccountRepository{
    private final SQLiteDatabase db;

    public AccountRepository(SQLiteDatabase db) {
        this.db = db;
    }

    // Insert an account into the database
    private void insertAccount(String accountName, String accountNumber, double balance, long userId) {
        ContentValues values = new ContentValues();
        values.put("AccountName", accountName);
        values.put("AccountNumber", accountNumber);
        values.put("Balance", balance);
        values.put("UserId", userId);
        db.insert(DBAdapter.TABLE_ACCOUNTS, null, values);

    }

    public List<String> getAccountNamesByUser(int userId) {
        List<String> accountNames = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT AccountName FROM " + DBAdapter.TABLE_ACCOUNTS + " WHERE UserId = ?", new String[]{String.valueOf(userId)});
        while (cursor.moveToNext()) {
            accountNames.add(cursor.getString(cursor.getColumnIndexOrThrow("AccountName")));
        }
        cursor.close();
        return accountNames;
    }

    public List<Account> getAccountsByUser(int userId, int transactionCount) {
        List<Account> accounts = new ArrayList<>();

        // 1. Query to get user's accounts
        Cursor accountCursor = db.rawQuery("SELECT * FROM " + DBAdapter.TABLE_ACCOUNTS + " WHERE UserId = ?", new String[]{String.valueOf(userId)});

        // 2. Iterate through accounts and get transactions
        while (accountCursor.moveToNext()) {
            String accountNumber = accountCursor.getString(accountCursor.getColumnIndexOrThrow("AccountNumber"));
            String accountName = accountCursor.getString(accountCursor.getColumnIndexOrThrow("AccountName"));
            double balance = accountCursor.getDouble(accountCursor.getColumnIndexOrThrow("Balance"));
            int userIdInAccount = accountCursor.getInt(accountCursor.getColumnIndexOrThrow("UserId"));

            // 3. Query to get transactions for this account
            Cursor transactionCursor = db.rawQuery("SELECT * FROM " + DBAdapter.TABLE_TRANSACTIONS
                            + " WHERE AccountNumber = ? ORDER BY TransactionId DESC LIMIT ?",
                    new String[]{accountNumber, String.valueOf(transactionCount)});

            List<Transaction> transactions = new ArrayList<>();
            while (transactionCursor.moveToNext()) {
                int transactionId = transactionCursor.getInt(transactionCursor.getColumnIndexOrThrow("TransactionId"));
                String reference = transactionCursor.getString(transactionCursor.getColumnIndexOrThrow("Reference"));
                String dateTime = transactionCursor.getString(transactionCursor.getColumnIndexOrThrow("DateTime"));
                double amount = transactionCursor.getDouble(transactionCursor.getColumnIndexOrThrow("Amount"));
                String type = transactionCursor.getString(transactionCursor.getColumnIndexOrThrow("Type"));
                double transactionBalance = transactionCursor.getDouble(transactionCursor.getColumnIndexOrThrow("Balance")); // Balance after transaction

                transactions.add(new Transaction(transactionId, reference, dateTime, amount, type, transactionBalance, accountNumber));
            }
            transactionCursor.close();

            // 4. Create Account object and add to list
            accounts.add(new Account(accountNumber, accountName, balance, userIdInAccount, transactions));
        }
        accountCursor.close();

        return accounts;
    }
}
