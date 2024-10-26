package moloi.tk.kovsiecash;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;


public class DBAdapter {
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

    // Repository Variables
    private UserRepository userRepository;
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;

    public UserRepository getUserRepository() {
        if (userRepository == null) {
            userRepository = new UserRepository(db);
        }
        return userRepository;
    }

    public AccountRepository getAccountRepository() {
        if (accountRepository == null) {
            accountRepository = new AccountRepository(db);
        }
        return accountRepository;
    }

    public TransactionRepository getTransactionRepository() {
        if (transactionRepository == null) {
            transactionRepository = new TransactionRepository(db);  

        }
        return transactionRepository;
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

    // Check if the database is already populated
    public boolean isDatabasePopulated() {
        open();  // Ensure the database is open
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        cursor.moveToFirst(); // Move to the first row
        int count = cursor.getInt(0); // Get the count from the first column
        cursor.close(); // Close the cursor
        return count > 0; // Return true if count is greater than 0
    }

    // Populate database if not already populated
    public void populateDatabase() throws IOException {
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

    // Create random accounts for a user
    private void createRandomAccounts(long userId) {
        Random random = new Random();
        int accountCount = random.nextInt(2) + 1; // Create 1 or 2 accounts

        // First account (Current)
        insertAccount("Current", generateAccountNumber(), 0, userId);

        // Second account (Savings), if applicable
        if (accountCount == 2) {
            insertAccount("Savings", generateAccountNumber(), 0, userId);
        }
    }


    // Populate transactions and notifications
    private void populateTransactionsAndNotifications() {
        Cursor cursor = db.query(TABLE_ACCOUNTS, null, null, null, null, null, null);
        Random random = new Random();

        while (cursor.moveToNext()) {
            String accountNumber = cursor.getString(cursor.getColumnIndexOrThrow("AccountNumber"));            double balance = 0;
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("UserId"));

            // Initial deposit
            double initialDeposit = 100 + (random.nextDouble() * 900);
            balance += initialDeposit;
            insertTransaction(userId, accountNumber, "Initial Deposit", initialDeposit, "Deposit");

            // Random transactions
            int transactionCount = random.nextInt(50) + 1;
            for (int i = 0; i < transactionCount; i++) {
                boolean isWithdrawal = random.nextBoolean();
                double amount = random.nextDouble() * balance / 2;
                if (isWithdrawal && amount <= balance) {
                    balance -= amount;
                    insertTransaction(userId, accountNumber, "Withdrawal", amount, "Withdrawal");
                } else {
                    amount = 100 + (random.nextDouble() * 900);
                    balance += amount;
                    insertTransaction(userId, accountNumber, "Deposit", amount, "Deposit");
                }
            }
        }
        cursor.close();
    }

    // Insert a notification
    private void insertNotification(int userId, String description, String notiDate, String type) {
        ContentValues values = new ContentValues();
        values.put("UserID", userId);
        values.put("NotificationDescription", description);
        values.put("NotificationDateTime", notiDate);
        values.put("Type", type);
        values.put("Status", 0); // 0 for unread
        db.insert(TABLE_NOTIFICATIONS, null, values);
    }

    // Generate a random account number
    private String generateAccountNumber() {
        Random random = new Random();
        return String.format("%010d", random.nextInt(1000000000));
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
            "FOREIGN KEY(UserId) REFERENCES " + TABLE_USERS + "(Id));";

    // SQL to create Transactions table
    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE " + TABLE_TRANSACTIONS + "(" +
            "TransactionId INTEGER PRIMARY KEY AUTOINCREMENT," +
            "Reference TEXT NOT NULL," +
            "DateTime TEXT NOT NULL," +
            "Amount REAL NOT NULL," +
            "Type TEXT NOT NULL," +
            "Balance REAL NOT NULL," +
            "AccountNumber TEXT NOT NULL," +
            "FOREIGN KEY(AccountNumber) REFERENCES " + TABLE_ACCOUNTS + "(AccountNumber));";

    // SQL to create Notifications table
    private static final String CREATE_TABLE_NOTIFICATIONS = "CREATE TABLE " + TABLE_NOTIFICATIONS + "(" +
            "NotificationID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "Type TEXT NOT NULL," +
            "NotificationDescription TEXT NOT NULL," +
            "NotificationDateTime TEXT NOT NULL," +
            "Status INTEGER NOT NULL," +
            "UserID INTEGER NOT NULL," +
            "FOREIGN KEY(UserID) REFERENCES " + TABLE_USERS + "(Id));";

    // SQL to create Advice table
    private static final String CREATE_TABLE_ADVICE = "CREATE TABLE " + TABLE_ADVICE + "(" +
            "AdviceID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "AdviceTitle TEXT NOT NULL," +
            "AdviceDescription TEXT NOT NULL," +
            "AdviceDate TEXT NOT NULL," +
            "AdviserId INTEGER NOT NULL," +
            "AdviseeId INTEGER NOT NULL," +
            "FOREIGN KEY(AdviserId) REFERENCES " + TABLE_USERS + "(Id)," +
            "FOREIGN KEY(AdviseeId) REFERENCES " + TABLE_USERS + "(Id));";

    // SQL to create Beneficiaries table
    private static final String CREATE_TABLE_BENEFICIARIES = "CREATE TABLE " + TABLE_BENEFICIARIES + "(" +
            "BeneficiaryId INTEGER PRIMARY KEY AUTOINCREMENT," +
            "BeneficiaryName TEXT NOT NULL," +
            "AccountNumbers TEXT NOT NULL," +
            "UserId INTEGER NOT NULL," +
            "FOREIGN KEY(UserId) REFERENCES " + TABLE_USERS + "(Id));";

    // SQL to create Reviews table
    private static final String CREATE_TABLE_REVIEWS = "CREATE TABLE " + TABLE_REVIEWS + "(" +
            "ReviewId INTEGER PRIMARY KEY AUTOINCREMENT," +
            "ReviewTitle TEXT NOT NULL," +
            "ReviewDescription TEXT NOT NULL," +
            "ReviewRating INTEGER NOT NULL," +
            "ReviewDate TEXT NOT NULL," +
            "UserId INTEGER NOT NULL," +
            "FOREIGN KEY(UserId) REFERENCES " + TABLE_USERS + "(Id));";

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
}





