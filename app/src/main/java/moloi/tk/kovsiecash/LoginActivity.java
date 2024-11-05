package moloi.tk.kovsiecash;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    // Declare Variables
    private DBAdapter dbAdapter;

    EditText edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get Instance of Database
        dbAdapter = DBAdapter.getInstance(this);
        dbAdapter.open();

        // Check if user is already logged in
        String[] arrRememberMe = checkForRememberMeFile();

        if (arrRememberMe.length > 1)
        {
            String strEmail = arrRememberMe[0];
            String strPassword = arrRememberMe[1];

            String userRole = dbAdapter.logIn(strEmail, strPassword);

            if (userRole.isBlank())
            {
                // Login failed
                Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            } else
            {
                // Login success
                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                CheckBox chkRemeberMe = findViewById(R.id.chkRemeberMe);
                if (chkRemeberMe.isChecked()) {

                }

                // Get the user's ID
                int userId = dbAdapter.getUserId(strEmail); // Or however you get the ID

                if (userRole.equals("Customer"))
                {
                    // Create the intent and add the user ID
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USER_ID", userId);
                    dbAdapter.close();
                    startActivity(intent);
                    finish();
                } else
                {
                    // Create the intent and add the user ID
                    Intent intent = new Intent(LoginActivity.this, MainAdminActivity.class);
                    intent.putExtra("USER_ID", userId);
                    intent.putExtra("USER_ROLE", userRole);
                    dbAdapter.close();
                    startActivity(intent);
                    finish();
                }
            }
        }

        // Declare and Instantiate components
        Button btnLogin = findViewById(R.id.btnLogin);
        EditText edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        TextView txtRegister = findViewById(R.id.txtRegisterAccount);
        TextView txtForgotPassword = findViewById(R.id.txtForgotPassword);
        txtForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Password Reset Coming Soon!!!", Toast.LENGTH_SHORT).show();

        });

        txtRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String strEmail = edtEmail.getText().toString();
            String strPassword = edtPassword.getText().toString();

            // Check if any of entries are empty
            if (strEmail.isEmpty() || strPassword.isEmpty()) {
                Toast.makeText(this, "Please enter an email and password to Log In", Toast.LENGTH_SHORT).show();
            }
            else {
                String userRole = dbAdapter.logIn(strEmail, strPassword);

                if (userRole.isBlank())
                {
                    // Login failed
                    Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                } else
                {
                    // Login success
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                    CheckBox chkRemeberMe = findViewById(R.id.chkRemeberMe);
                    if (chkRemeberMe.isChecked()) {
                        createRememberMeFile(strEmail, strPassword);
                    }

                    // Get the user's ID
                    int userId = dbAdapter.getUserId(strEmail); // Or however you get the ID

                    if (userRole.equals("Customer"))
                    {
                        // Create the intent and add the user ID
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("USER_ID", userId);
                        dbAdapter.close();
                        startActivity(intent);
                        finish();
                    } else
                    {
                        // Create the intent and add the user ID
                        Intent intent = new Intent(LoginActivity.this, MainAdminActivity.class);
                        intent.putExtra("USER_ID", userId);
                        intent.putExtra("USER_ROLE", userRole);
                        dbAdapter.close();
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });

        edtPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edtPassword.getRight() - edtPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // Your action here
                    togglePasswordVisibility();
                    return true;
                }
            }
            return false;
        });
    }

    public void createRememberMeFile(String username, String password) {
        try {
            File file = new File(this.getFilesDir(), "rememberme.txt"); // Create file object
            FileWriter writer = new FileWriter(file, false); // Overwrite if file exists

            // Write username and password to file
            writer.write(username + "," + password);
            writer.close();

            Toast.makeText(this, "You will not need to log in next time", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error creating the readme file", Toast.LENGTH_SHORT).show();
        }
    }

    public String[] checkForRememberMeFile() {
        File file = new File(this.getFilesDir(), "rememberme.txt");

        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                reader.close();

                if (line != null) {
                    String[] credentials = line.split(",");
                    return credentials; // Return username and password
                }
            } catch (IOException e) {
                Toast.makeText(this, "Error reading remember me file", Toast.LENGTH_SHORT).show();
            }
        }

        return new String[1]; // Return empty array if file doesn't exist or error occurs
    }


    private void togglePasswordVisibility() {
        if (edtPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
            edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); // Show password
            edtPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_password_hidden, 0); // Change drawable if needed
        } else {
            edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance()); // Hide password
            edtPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_password_visible, 0); // Change drawable if needed
        }

        // Move cursor to the end
        edtPassword.setSelection(edtPassword.getText().length());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close(); // Close the database connection to avoid leaks
    }
}