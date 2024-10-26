package moloi.tk.kovsiecash;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class TransactionRepository implements  ITransactionRepository{
    private final SQLiteDatabase db;

    public TransactionRepository(SQLiteDatabase db) {
        this.db = db;
    }

    // Insert a transaction
    private void insertTransaction(int userId, String accountNumber, String reference, double amount, String type) {
        // 1. Get current account balance
        double currentBalance = getAccountBalance(accountNumber);
        String date = getRandomDateBeforeToday();

        // 2. Calculate new balance based on transaction type
        double newBalance = type.equals("Deposit")
                ? currentBalance + amount
                : currentBalance - amount;

        // 3. Create ContentValues for transaction
        ContentValues transactionValues = new ContentValues();
        transactionValues.put("AccountNumber", accountNumber);
        transactionValues.put("Reference", reference);
        transactionValues.put("Balance", newBalance); // Updated balance
        transactionValues.put("Amount", amount);
        transactionValues.put("DateTime", date);
        transactionValues.put("Type", type);

        // 4. Insert transaction record
        db.insert(TABLE_TRANSACTIONS, null, transactionValues);

        // 5. Update account balance
        updateAccountBalance(accountNumber, newBalance);

        // 6. Insert notification
        insertNotification(userId, "Transaction " + type + reference + " of " + amount,
                date, type);
    }
    public ArrayList<Transaction> getTransactionsRecent(int userId, int transactionCount) {
        ArrayList<Transaction> transactions = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + DBAdapter.TABLE_TRANSACTIONS +
                        " WHERE AccountNumber IN (SELECT AccountNumber FROM " + DBAdapter.TABLE_ACCOUNTS + " WHERE UserId = ?)" +
                        " ORDER BY TransactionId DESC" +
                        " LIMIT ?",
                new String[]{String.valueOf(userId), String.valueOf(transactionCount)}
        );

        while (cursor.moveToNext()) {
            int transactionId = cursor.getInt(cursor.getColumnIndexOrThrow("TransactionId"));
            String reference = cursor.getString(cursor.getColumnIndexOrThrow("Reference"));
            String dateTime = cursor.getString(cursor.getColumnIndexOrThrow("DateTime"));
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("Amount"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("Type"));
            double balance = cursor.getDouble(cursor.getColumnIndexOrThrow("Balance"));
            String accountNumber = cursor.getString(cursor.getColumnIndexOrThrow("AccountNumber"));

            Transaction transaction = new Transaction(transactionId, reference, dateTime, amount, type, balance, accountNumber);
            transactions.add(transaction);
        }

        cursor.close();
        return transactions;
    }

    // Helper function to get account balance
    private double getAccountBalance(String accountNumber) {
        // Query the accounts table to get the balance for the given accountNumber
        Cursor cursor = db.rawQuery("SELECT Balance FROM " + TABLE_ACCOUNTS + " WHERE AccountNumber = ?", new String[]{accountNumber});
        if (cursor.moveToFirst()) {
            return cursor.getDouble(cursor.getColumnIndexOrThrow("Balance"));
        }
        cursor.close();
        return 0.0; // Or handle the case where account is not found
    }

    // Helper function to update account balance
    private void updateAccountBalance(String accountNumber, double newBalance) {
        ContentValues accountValues = new ContentValues();
        accountValues.put("Balance", newBalance);
        db.update(TABLE_ACCOUNTS, accountValues, "AccountNumber = ?", new String[]{accountNumber});
    }
    
}
