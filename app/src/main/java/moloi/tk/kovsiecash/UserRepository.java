package moloi.tk.kovsiecash;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserRepository implements IUserRepository {
    private final SQLiteDatabase db;

    public UserRepository(SQLiteDatabase db) {
        this.db = db;
    }

    // Insert a user into the database
    private long insertUser(String username, String email, String password, String role) {
        ContentValues values = new ContentValues();
        values.put("UserName", username);
        values.put("Email", email);
        values.put("Password", password);
        values.put("Role", role);
        return db.insert(DBAdapter.TABLE_USERS, null, values);
    }

    // Method to log in a user
    public boolean logIn(String email, String password) {
        try {
            String query = "SELECT * FROM " + DBAdapter.TABLE_USERS + " WHERE email = ? AND password = ? LIMIT 1";
            Cursor cursor = db.rawQuery(query, new String[]{email, password});

            boolean userExists = cursor.moveToFirst();
            cursor.close();
            return userExists;
        } catch (Exception e) {
            // Log the exception or print it
            e.printStackTrace();
            return false;
        }
    }

    public int getUserId(String email) {
        int userId = -1; // Default value if not found

        // 1. Query the database for the user with the given email
        Cursor cursor = db.rawQuery("SELECT Id FROM " + DBAdapter.TABLE_USERS + " WHERE Email = ?", new String[]{email});

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

        // 1. Query the database for the user with the given ID
        Cursor cursor = db.rawQuery("SELECT UserName FROM " + DBAdapter.TABLE_USERS + " WHERE Id = ?", new String[]{String.valueOf(userId)});

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
}
