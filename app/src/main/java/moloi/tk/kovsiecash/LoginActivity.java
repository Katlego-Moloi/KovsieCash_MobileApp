package moloi.tk.kovsiecash;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        Toast.makeText(this, "Yessirski Opened", Toast.LENGTH_SHORT).show();
        dbAdapter = DBAdapter.getInstance(this);
        dbAdapter.open();
        try {
            dbAdapter.populateDatabase();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Declare and Instantiate components
        Button btnLogin = findViewById(R.id.btnLogin);
        EditText edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        TextView txtRegister = findViewById(R.id.txtRegisterAccount);

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
            else if (dbAdapter.logIn(strEmail, strPassword)) {
                // Login success
                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                // Get the user's ID (assuming dbAdapter has a method for this)
                int userId = dbAdapter.getUserId(strEmail); // Or however you get the ID

                // Create the intent and add the user ID
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("USER_ID", userId);

                startActivity(intent);
                dbAdapter.close();
                finish();

            }  else {
                // Login failed
                Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
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