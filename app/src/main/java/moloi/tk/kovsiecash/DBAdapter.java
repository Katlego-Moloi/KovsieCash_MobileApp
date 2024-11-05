package moloi.tk.kovsiecash;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.OptIn;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;


public class DBAdapter {

    // Database details
    private static final String DATABASE_NAME = "KovsieCashDB";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USERS = "ApplicationUsers";
    public static final String TABLE_ACCOUNTS = "Account";
    public static final String TABLE_TRANSACTIONS = "Transactions";
    public static final String TABLE_NOTIFICATIONS = "Notifications";
    public static final String TABLE_ADVICE = "Advice";
    public static final String TABLE_BENEFICIARIES = "Beneficiaries";
    public static final String TABLE_REVIEWS = "Reviews";

    // SQL to create ApplicationUsers table
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "(" +
            "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "ProfilePicture TEXT," +
            "UserName TEXT," +
            "Email TEXT," +
            "Password TEXT," +
            "Role TEXT," +
            "PhoneNumber TEXT);";

    // SQL to create Account table
    private static final String CREATE_TABLE_ACCOUNTS = "CREATE TABLE " + TABLE_ACCOUNTS + "(" +
            "AccountNumber TEXT PRIMARY KEY," +
            "AccountName TEXT NOT NULL," +
            "Balance REAL NOT NULL," +
            "UserId INTEGER NOT NULL," +
            "FOREIGN KEY(UserId) REFERENCES " + TABLE_USERS + "(Id) ON DELETE CASCADE);";

    // SQL to create Transactions table
    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE " + TABLE_TRANSACTIONS + "(" +
            "TransactionId INTEGER PRIMARY KEY AUTOINCREMENT," +
            "Reference TEXT NOT NULL," +
            "DateTime TEXT NOT NULL," +
            "Amount REAL NOT NULL," +
            "Type TEXT NOT NULL," +
            "Balance REAL NOT NULL," +
            "AccountNumber TEXT NOT NULL," +
            "FOREIGN KEY(AccountNumber) REFERENCES " + TABLE_ACCOUNTS + "(AccountNumber) ON DELETE CASCADE);";

    // SQL to create Notifications table
    private static final String CREATE_TABLE_NOTIFICATIONS = "CREATE TABLE " + TABLE_NOTIFICATIONS + "(" +
            "NotificationID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "Type TEXT NOT NULL," +
            "NotificationDescription TEXT NOT NULL," +
            "NotificationDateTime TEXT NOT NULL," +
            "Status INTEGER NOT NULL," +
            "UserID INTEGER NOT NULL," +
            "FOREIGN KEY(UserID) REFERENCES " + TABLE_USERS + "(Id) ON DELETE CASCADE);";

    // SQL to create Advice table
    private static final String CREATE_TABLE_ADVICE = "CREATE TABLE " + TABLE_ADVICE + "(" +
            "AdviceID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "AdviceTitle TEXT NOT NULL," +
            "AdviceDescription TEXT NOT NULL," +
            "AdviceDate TEXT NOT NULL," +
            "AdviserId INTEGER NOT NULL," +
            "AdviseeId INTEGER NOT NULL," +
            "FOREIGN KEY(AdviserId) REFERENCES " + TABLE_USERS + "(Id) ON DELETE CASCADE," +
            "FOREIGN KEY(AdviseeId) REFERENCES " + TABLE_USERS + "(Id) ON DELETE CASCADE);";

    // SQL to create Beneficiaries table
    private static final String CREATE_TABLE_BENEFICIARIES = "CREATE TABLE " + TABLE_BENEFICIARIES + "(" +
            "BeneficiaryId INTEGER PRIMARY KEY AUTOINCREMENT," +
            "BeneficiaryName TEXT NOT NULL," +
            "AccountNumbers TEXT NOT NULL," +
            "UserId INTEGER NOT NULL," +
            "FOREIGN KEY(UserId) REFERENCES " + TABLE_USERS + "(Id) ON DELETE CASCADE);";

    // SQL to create Reviews table
    private static final String CREATE_TABLE_REVIEWS = "CREATE TABLE " + TABLE_REVIEWS + "(" +
            "ReviewId INTEGER PRIMARY KEY AUTOINCREMENT," +
            "ReviewTitle TEXT NOT NULL," +
            "ReviewDescription TEXT NOT NULL," +
            "ReviewRating INTEGER NOT NULL," +
            "ReviewDate TEXT NOT NULL," +
            "UserId INTEGER NOT NULL," +
            "FOREIGN KEY(UserId) REFERENCES " + TABLE_USERS + "(Id) ON DELETE CASCADE);";

    // Helper class to manage the database
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create all tables
            db.execSQL(CREATE_TABLE_USERS);
            db.execSQL(CREATE_TABLE_ACCOUNTS);
            db.execSQL(CREATE_TABLE_TRANSACTIONS);
            db.execSQL(CREATE_TABLE_NOTIFICATIONS);
            db.execSQL(CREATE_TABLE_ADVICE);
            db.execSQL(CREATE_TABLE_BENEFICIARIES);
            db.execSQL(CREATE_TABLE_REVIEWS);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Drop old tables and create new ones
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADVICE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BENEFICIARIES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
            onCreate(db);
        }
    }

    // Singleton Database Instance Variables
    private static DBAdapter instance;
    private final Context context;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    // Private constructor prevents instantiation from other classes
    private DBAdapter(Context context) {
        this.context = context.getApplicationContext(); // Use app context to avoid leaks
        dbHelper = new DatabaseHelper(this.context);
    }

    // Singleton pattern to ensure only one instance is created
    public static synchronized DBAdapter getInstance(Context context) {
        if (instance == null) {
            instance = new DBAdapter(context);
        }
        return instance;
    }

    // Open the database connection
    public void open() {
        if (db == null || !db.isOpen()) {
            db = dbHelper.getWritableDatabase();
        }
    }

    // Close the database connection
    public void close() {
        if (db != null && db.isOpen()) {
            dbHelper.close();
        }
    }

    // Method to log in a user
    public String logIn(String email, String password) {
        String userRole = "";

        open();  // Ensure the database is open
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE email = ? AND password = ? LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});

        if (cursor.moveToFirst()) {
            // 3. Get the user ID from the cursor
            userRole = cursor.getString(cursor.getColumnIndexOrThrow("Role"));
        }

        cursor.close();

        return userRole;
    }

    public int getUserId(String email) {
        open();
        int userId = -1; // Default value if not found

        // 1. Query the database for the user with the given email
        Cursor cursor = db.rawQuery("SELECT Id FROM " + TABLE_USERS + " WHERE Email = ?", new String[]{email});

        // 2. Check if a user was found
        if (cursor.moveToFirst()) {
            // 3. Get the user ID from the cursor
            userId = cursor.getInt(cursor.getColumnIndexOrThrow("Id"));
        }

        // 4. Close the cursor
        cursor.close();

        // 5. Return the user ID (or -1 if not found)
        return userId;
    }

    public String getUserName(int userId) {
        String userName = null; // Default value if not found
        open();

        // 1. Query the database for the user with the given ID
        Cursor cursor = db.rawQuery("SELECT UserName FROM " + TABLE_USERS + " WHERE Id = ?", new String[]{String.valueOf(userId)});

        // 2. Check if a user was found
        if (cursor.moveToFirst()) {
            // 3. Get the username from the cursor
            userName = cursor.getString(cursor.getColumnIndexOrThrow("UserName"));
        }

        // 4. Close the cursor
        cursor.close();

        // 5. Return the username (or null if not found)
        return userName;
    }

    public String getUserEmail(int userId) {
        open();
        String userName = null; // Default value if not found

        // 1. Query the database for the user with the given ID
        Cursor cursor = db.rawQuery("SELECT Email FROM " + TABLE_USERS + " WHERE Id = ?", new String[]{String.valueOf(userId)});

        // 2. Check if a user was found
        if (cursor.moveToFirst()) {
            // 3. Get the username from the cursor
            userName = cursor.getString(cursor.getColumnIndexOrThrow("Email"));
        }

        // 4. Close the cursor
        cursor.close();

        // 5. Return the username (or null if not found)
        return userName;
    }

    public ArrayList<Transaction> getRecentTransactions(int userId, int transactionCount) {
        open();
        ArrayList<Transaction> transactions = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_TRANSACTIONS +
                        " WHERE AccountNumber IN (SELECT AccountNumber FROM " + TABLE_ACCOUNTS + " WHERE UserId = ?)" +
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

    public ArrayList<Customer> getAllCustomers() {
        open();
        ArrayList<Customer> customers = new ArrayList<>();

        // Query to get all customers
        String customerQuery = "SELECT * FROM " + TABLE_USERS + " WHERE Role = 'Customer' ORDER BY Id DESC";
        Cursor customerCursor = db.rawQuery(customerQuery, null);

        if (customerCursor.moveToFirst()) {
            do {
                int userId = customerCursor.getInt(customerCursor.getColumnIndexOrThrow("Id"));
                String username = customerCursor.getString(customerCursor.getColumnIndexOrThrow("UserName"));
                String email = customerCursor.getString(customerCursor.getColumnIndexOrThrow("Email"));

                // Get accounts for the current customer
                ArrayList<Account> accounts = (ArrayList<Account>) getUserAccounts(userId, 2);

                // Create a Customer object and add it to the list
                Customer customer = new Customer(userId, username, email, accounts);
                customers.add(customer);
            } while (customerCursor.moveToNext());
        }

        customerCursor.close();
        db.close();
        return customers;
    }

    public List<String> getAccountNames(int userId) {
        open();
        List<String> accountNames = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT AccountName FROM " + TABLE_ACCOUNTS + " WHERE UserId = ?", new String[]{String.valueOf(userId)});
        while (cursor.moveToNext()) {
            accountNames.add(cursor.getString(cursor.getColumnIndexOrThrow("AccountName")));
        }
        cursor.close();
        return accountNames;
    }

    public List<Account> getUserAccounts(int userId, int transactionCount) {
        List<Account> accounts = new ArrayList<>();
        open();

        // 1. Query to get user's accounts
        Cursor accountCursor = db.rawQuery("SELECT * FROM " + TABLE_ACCOUNTS + " WHERE UserId = ?", new String[]{String.valueOf(userId)});

        // 2. Iterate through accounts and get transactions
        while (accountCursor.moveToNext()) {
            String accountNumber = accountCursor.getString(accountCursor.getColumnIndexOrThrow("AccountNumber"));
            String accountName = accountCursor.getString(accountCursor.getColumnIndexOrThrow("AccountName"));
            double balance = accountCursor.getDouble(accountCursor.getColumnIndexOrThrow("Balance"));
            int userIdInAccount = accountCursor.getInt(accountCursor.getColumnIndexOrThrow("UserId"));

            // 3. Query to get transactions for this account
            Cursor transactionCursor = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS
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

    public Account getAccountByAccountNumber(String accountNumber, int transactionCount) {
        open();

        // 1. Query to get user's accounts
        Cursor accountCursor = db.rawQuery("SELECT * FROM " + TABLE_ACCOUNTS + " WHERE AccountNumber = ?", new String[]{String.valueOf(accountNumber)});

        // 2. Iterate through accounts and get transactions
        accountCursor.moveToNext();
        String accountName = accountCursor.getString(accountCursor.getColumnIndexOrThrow("AccountName"));
        double balance = accountCursor.getDouble(accountCursor.getColumnIndexOrThrow("Balance"));
        int userIdInAccount = accountCursor.getInt(accountCursor.getColumnIndexOrThrow("UserId"));

        // 3. Query to get transactions for this account
        Cursor transactionCursor = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS
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
        accountCursor.close();

        return new Account(accountNumber, accountName, balance, userIdInAccount, transactions);
    }

    public boolean CheckForAccount(String accountNumber)
    {
        open();
        boolean accountFound = false; // Default value if not found

        Cursor cursor = db.rawQuery("SELECT AccountName FROM " + TABLE_ACCOUNTS + " WHERE AccountNumber = ?", new String[]{String.valueOf(accountNumber)});

        if (cursor.moveToFirst()) {
            accountFound = true;
        }

        return accountFound;
    }

    // Check if the database is already populated
    public boolean isDatabasePopulated() {
        open();
        open();  // Ensure the database is open
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        cursor.moveToFirst(); // Move to the first row
        int count = cursor.getInt(0); // Get the count from the first column
        cursor.close(); // Close the cursor
        return count > 0; // Return true if count is greater than 0
    }

    // Populate database if not already populated
    public void populateDatabase() throws IOException {
        open();
        if (isDatabasePopulated()) {
            return; // Exit if the database is already populated
        }

        // Populate users and accounts from a text file
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("users.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] userData = line.split(",");
            if (userData.length == 4) {
                String username = userData[0].trim();
                String email = userData[1].trim();
                String password = userData[2].trim();
                String role = userData[3].trim();

                // Insert user into the database
                long userId = insertUser(username, email, password, role);

                // Create 1-2 accounts for the user
                createRandomAccounts(userId);
            }
        }

        // Populate transactions and notifications
        populateTransactionsAndNotifications();
    }

    // Insert a user into the database
    public long insertUser(String username, String email, String password, String role) {
        open();
        ContentValues values = new ContentValues();
        values.put("UserName", username);
        values.put("Email", email);
        values.put("Password", password);
        values.put("Role", role);
        return db.insert(TABLE_USERS, null, values);

    }

    public int updateUserDetails(int userId, String username, String email, String password, String role) {
        open();
        ContentValues values = new ContentValues();

        // Put the updated values into the ContentValues object
        values.put("UserName", username);
        values.put("Email", email);
        values.put("Password", password);
        values.put("Role", role);

        // Define the WHERE clause to specify which row to update
        String whereClause = "Id = ?";
        String[] whereArgs = {String.valueOf(userId)};

        // Perform the update
        int rowsAffected = db.update(TABLE_USERS, values, whereClause, whereArgs);
        db.close();
        return rowsAffected;
    }


    public void deleteUserById(int userId) {
        open();
        String whereClause = "Id = ?"; // Assuming 'Id' is the primary key column for users
        String[] whereArgs = {String.valueOf(userId)};

        int rowsAffected = db.delete(TABLE_USERS, whereClause, whereArgs);

        db.close();
    }


    // Create random accounts for a user
    private void createRandomAccounts(long userId) {
        open();
        Random random = new Random();
        int accountCount = random.nextInt(2) + 1; // Create 1 or 2 accounts

        // First account (Current)
        insertAccount("Current", generateAccountNumber(), 0, userId);

        // Second account (Savings), if applicable
        if (accountCount == 2) {
            insertAccount("Savings", generateAccountNumber(), 0, userId);
        }
    }

    // Insert an account into the database
    public void insertAccount(String accountName, String accountNumber, double balance, long userId) {
        open();
        ContentValues values = new ContentValues();
        values.put("AccountName", accountName);
        values.put("AccountNumber", accountNumber);
        values.put("Balance", balance);
        values.put("UserId", userId);
        db.insert(TABLE_ACCOUNTS, null, values);
    }

    public void insertAccountByUserId(long userId, String accountName) {
        open();
        insertAccount(accountName, generateAccountNumber(), 0, userId);
    }

    // Populate transactions and notifications
    private void populateTransactionsAndNotifications() {
        open();
        Cursor cursor = db.query(TABLE_ACCOUNTS, null, null, null, null, null, null);
        Random random = new Random();

        while (cursor.moveToNext()) {
            String accountNumber = cursor.getString(cursor.getColumnIndexOrThrow("AccountNumber"));
            double balance = 0;
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("UserId"));

            // Initial deposit
            double initialDeposit = 100 + (random.nextDouble() * 900);
            balance += initialDeposit;
            updateAccountBalance(accountNumber, 0);
            insertTransaction(userId, accountNumber, "Initial Deposit", initialDeposit, "Deposit", "2024-01-01");

            // Random transactions
            int transactionCount = random.nextInt(50) + 1;
            String[] arrRandomDates = new String[transactionCount];

            for (int i = 0; i < transactionCount; i++)
                arrRandomDates[i] = getRandomDateBeforeToday();

            Arrays.sort(arrRandomDates);

            for (int i = 0; i < transactionCount; i++) {
                boolean isWithdrawal = random.nextBoolean();
                double amount = random.nextDouble() * balance / 2;
                if (isWithdrawal && amount <= balance) {
                    balance -= amount;
                    insertTransaction(userId, accountNumber, "", amount, "Withdrawal", arrRandomDates[i]);
                } else {
                    amount = 100 + (random.nextDouble() * 900);
                    balance += amount;
                    insertTransaction(userId, accountNumber, "", amount, "Deposit", arrRandomDates[i]);
                }
            }
        }
        cursor.close();
    }

    // Insert a transaction
    private void insertTransaction(int userId, String accountNumber, String reference, double amount, String type, String date) {
        open();
        // 1. Get current account balance
        double currentBalance = getAccountBalance(accountNumber);

        if (date.isBlank())
        {
            date = getRandomDateBeforeToday();
        }

        reference = reference.isBlank()? generateReference(type):reference;

        // 2. Calculate new balance based on transaction type
        double newBalance = type.equals("Deposit")
                ? currentBalance + amount
                : currentBalance - amount;

        // 3. Create ContentValues for transaction
        ContentValues transactionValues = new ContentValues();
        transactionValues.put("AccountNumber", accountNumber);
        transactionValues.put("Reference", reference);
        transactionValues.put("Balance", newBalance); // Updated balance
        transactionValues.put("Amount", type.equals("Deposit")? +amount : -amount);
        transactionValues.put("DateTime", date);
        transactionValues.put("Type", type);

        // 4. Insert transaction record
        db.insert(TABLE_TRANSACTIONS, null, transactionValues);

        // 5. Update account balance
        updateAccountBalance(accountNumber, newBalance);

        // 6. Insert notification
        Locale locale = new Locale("en", "ZA");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        insertNotification(userId, type + ": " + reference + "\n" +  currencyFormatter.format(amount),
                date, type);
    }

    public int getUserByAccountNumber(String accountNumber) {
        open();
        int userId = -1; // Default value if not found

        // 1. Query the database for the user with the given email
        Cursor cursor = db.rawQuery("SELECT UserId FROM " + TABLE_ACCOUNTS + " WHERE AccountNumber = ?", new String[]{accountNumber});

        // 2. Check if a user was found
        if (cursor.moveToFirst()) {
            // 3. Get the user ID from the cursor
            userId = cursor.getInt(cursor.getColumnIndexOrThrow("UserId"));
        }

        // 4. Close the cursor
        cursor.close();

        // 5. Return the user ID (or -1 if not found)
        return userId;
    }

    public void insertPayment(int userIdFrom, int userIdTo, String accountNumberFrom, String accountNumberTo,
                                   String reference, double amount) {
        open();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        insertTransaction(userIdFrom, accountNumberFrom, reference, amount, "Withdrawal",
                LocalDate.now(ZoneId.systemDefault()).format(formatter));
        insertTransaction(userIdTo, accountNumberTo, reference, amount, "Deposit",
                LocalDate.now(ZoneId.systemDefault()).format(formatter));
    }

    public void insertTransfer(int userId, String accountNumberFrom, String accountNumberTo,
                               String reference, double amount) {
        open();
        // 1. Get current account balance
        double currentBalance = getAccountBalance(accountNumberFrom);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 2. Calculate new balance based on transaction type
        double newBalance = currentBalance - amount;

        // 3. Create ContentValues for transaction
        ContentValues transactionValues = new ContentValues();
        transactionValues.put("AccountNumber", accountNumberFrom);
        transactionValues.put("Reference", reference.isBlank()? generateReference("Transfer"):reference);
        transactionValues.put("Balance", newBalance);
        transactionValues.put("Amount", -amount);
        transactionValues.put("DateTime", LocalDate.now(ZoneId.systemDefault()).format(formatter));
        transactionValues.put("Type", "Transfer");

        // 4. Insert transaction record
        db.insert(TABLE_TRANSACTIONS, null, transactionValues);

        // 5. Update account balance
        updateAccountBalance(accountNumberFrom, newBalance);

        // 6. Insert notification
        Locale locale = new Locale("en", "ZA");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        insertNotification(userId, "Transfer: " + reference + "\n" + currencyFormatter.format(amount),
                LocalDate.now(ZoneId.systemDefault()).format(formatter), "Transfer");

        // 1. Get current account balance
        currentBalance = getAccountBalance(accountNumberTo);

        // 2. Calculate new balance based on transaction type
        newBalance = currentBalance + amount;

        // 3. Create ContentValues for transaction
        transactionValues = new ContentValues();
        transactionValues.put("AccountNumber", accountNumberTo);
        transactionValues.put("Reference", reference.isBlank()? generateReference("Transfer"):reference);
        transactionValues.put("Balance", newBalance); // Updated balance
        transactionValues.put("Amount", +amount);
        transactionValues.put("DateTime", LocalDate.now(ZoneId.systemDefault()).format(formatter));
        transactionValues.put("Type", "Transfer");

        // 4. Insert transaction record
        db.insert(TABLE_TRANSACTIONS, null, transactionValues);

        // 5. Update account balance
        updateAccountBalance(accountNumberTo, newBalance);
    }

    public void setNotificationRead(int notificationId) {
        open();
        ContentValues values = new ContentValues();
        values.put("Status", 1);

        String whereClause = "NotificationId = ?";
        String[] whereArgs = {String.valueOf(notificationId)};

        int rowsAffected = db.update(TABLE_NOTIFICATIONS, values, whereClause, whereArgs);

        db.close();
    }

    public void insertAdvice(String adviceTitle, String adviceDescription, String adviceDate, int adviserId, int adviseeId) {
        open();
        ContentValues values = new ContentValues();
        values.put("AdviceTitle", adviceTitle);
        values.put("AdviceDescription", adviceDescription);
        values.put("AdviceDate", adviceDate);
        values.put("AdviserId", adviserId);
        values.put("AdviseeId", adviseeId);

        long adviceId = db.insert(TABLE_ADVICE, null, values);

        // Create notification for the advisee
        String notificationDescription = adviceTitle + " - " + adviceId;
        insertNotification(adviseeId, notificationDescription, adviceDate, "Advice");
    }


    public void updateUserRole(int userID, String role) {
        open();
        ContentValues values = new ContentValues();
        values.put("Role", role);

        String whereClause = "Id = ?";
        String[] whereArgs = {String.valueOf(userID)};

        int rowsAffected = db.update(TABLE_USERS, values, whereClause, whereArgs);

        db.close();
    }

    public int countUnreadNotis(int userId) {
        open();
        String query = "SELECT COUNT(*) FROM " + TABLE_NOTIFICATIONS + " WHERE UserID = ? AND Status = 0"; // Assuming 'Status = 0' means unread
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        int unreadCount = 0;
        if (cursor.moveToFirst()) {
            unreadCount = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return unreadCount;
    }

    public ArrayList<Notification> getUnreadNotis(int userId) {
        open();
        ArrayList<Notification> unreadNotifications = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NOTIFICATIONS + " WHERE UserID = ? AND Status = 0 ORDER BY NotificationId DESC"; // Assuming 'Status = 0' means unread
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int notificationID = cursor.getInt(cursor.getColumnIndexOrThrow("NotificationID"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("Type"));
                String notificationDescription = cursor.getString(cursor.getColumnIndexOrThrow("NotificationDescription"));
                String notificationDateTime = cursor.getString(cursor.getColumnIndexOrThrow("NotificationDateTime"));
                int status = cursor.getInt(cursor.getColumnIndexOrThrow("Status"));
                int userID = cursor.getInt(cursor.getColumnIndexOrThrow("UserID"));

                Notification notification = new Notification(notificationID, type, notificationDescription, notificationDateTime, status, userID);
                unreadNotifications.add(notification);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return unreadNotifications;
    }

    public String[] getAdvice(int adviceID) {
        open();
        String query = "SELECT AdviceTitle, AdviceDescription FROM " + TABLE_ADVICE + " WHERE AdviceID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(adviceID)});

        String[] adviceDetails = new String[2]; // Array to store title and description

        if (cursor.moveToFirst()) {
            adviceDetails[0] = cursor.getString(cursor.getColumnIndexOrThrow("AdviceTitle"));
            adviceDetails[1] = cursor.getString(cursor.getColumnIndexOrThrow("AdviceDescription"));
        }

        cursor.close();
        return adviceDetails;
    }


    public void setUsersNotisRead(int userId) {
        open();
        ContentValues values = new ContentValues();
        values.put("Status", 1);

        String whereClause = "UserID = ? AND Type != ? AND Type != ?";
        String[] whereArgs = {String.valueOf(userId), "RoleChange", "Advice"};

        int rowsAffected = db.update(TABLE_NOTIFICATIONS, values, whereClause, whereArgs);

        close();
    }

    public String getUserRole(int userId) {
        open();
        String query = "SELECT Role FROM " + TABLE_USERS + " WHERE Id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        String role = null;
        if (cursor.moveToFirst()) {
            role = cursor.getString(cursor.getColumnIndexOrThrow("Role"));
        }

        cursor.close();
        db.close();
        return role;
    }


    // Helper function to get account balance
    private double getAccountBalance(String accountNumber) {
        // Query the accounts table to get the balance for the given accountNumber
        open();
        Cursor cursor = db.rawQuery("SELECT Balance FROM " + TABLE_ACCOUNTS + " WHERE AccountNumber = ?", new String[]{accountNumber});
        if (cursor.moveToFirst()) {
            return cursor.getDouble(cursor.getColumnIndexOrThrow("Balance"));
        }
        cursor.close();

        return 0.0; // Or handle the case where account is not found
    }

    // Helper function to update account balance
    private void updateAccountBalance(String accountNumber, double newBalance) {
        open();
        ContentValues accountValues = new ContentValues();
        accountValues.put("Balance", newBalance);
        db.update(TABLE_ACCOUNTS, accountValues, "AccountNumber = ?", new String[]{accountNumber});
    }

    // Insert a notification
    public void insertNotification(int userId, String description, String notiDate, String type) {
        open();
        ContentValues values = new ContentValues();
        values.put("UserID", userId);
        values.put("NotificationDescription", description);
        values.put("NotificationDateTime", notiDate);
        values.put("Type", type);
        values.put("Status", 0);
        db.insert(TABLE_NOTIFICATIONS, null, values);
    }

    // Generate a random account number
    private String generateAccountNumber() {
        Random random = new Random();
        return String.format("%010d", random.nextInt(1000000000));
    }

    public static String generateReference(String transactionType) {
        // Get the current date and time in the format "yyyyMMddHHmmss"
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = dateFormat.format(new Date());

        // Generate a random 4-digit number for extra uniqueness
        Random random = new Random();
        int randomDigits = 1000 + random.nextInt(9000);

        // Determine the abbreviation for the transaction type
        String transactionAbbreviation;
        switch (transactionType.toLowerCase()) {
            case "transfer":
                transactionAbbreviation = "TRF";
                break;
            case "deposit":
                transactionAbbreviation = "DEP";
                break;
            case "withdrawal":
                transactionAbbreviation = "WTH";
                break;
            default:
                transactionAbbreviation = "UNK";
                break;
        }

        // Combine the components into a single reference string
        return transactionAbbreviation + "-" + timestamp + "-" + randomDigits;
    }

    public double[] getDebitCreditForCurrentMonth(String accountNumber) {
        double[] arrDouble = new double[2];

        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Month is 0-indexed

        Account account = getAccountByAccountNumber(accountNumber, 30);
        arrDouble[0] = 0;
        arrDouble[1] = 0;

        for (Transaction transaction: account.getTransactions()) {
            if (transaction.getDateTime().contains("-" + String.valueOf(currentMonth) + "-"))
            {
                if (transaction.getAmount() < 0)
                {
                    arrDouble[0] -= transaction.getAmount();
                }
                else
                {
                    arrDouble[1] += transaction.getAmount();
                }
            }
        }

        return arrDouble;
    }

    public static String getRandomDateBeforeToday() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        int currentYear = today.getYear(); // Get current year
        LocalDate startOfYear = LocalDate.of(currentYear, 1, 1);
        long daysBetween = today.toEpochDay() - startOfYear.toEpochDay();

        if (daysBetween <= 0) {
            return null; // No dates before today in the current year
        }

        long randomDays = ThreadLocalRandom.current().nextLong(daysBetween);
        LocalDate randomDate = startOfYear.plusDays(randomDays);

        // Format the date as a string (e.g., "yyyy-MM-dd")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return randomDate.format(formatter);
    }
}

