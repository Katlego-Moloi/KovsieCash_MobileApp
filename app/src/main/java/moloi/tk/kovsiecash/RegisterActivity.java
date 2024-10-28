package moloi.tk.kovsiecash;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.TextUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {
    // Declare Variables
    private DBAdapter dbAdapter;
    private EditText edtIdNumber, edtUsername, edtEmail, edtPassword;
    private Button btnRegister;
    private RadioGroup radgrpUserRole;
    private UserRoleEnum customerRole = UserRoleEnum.CUSTOMER;
    private TextView txtBackToLogin;

    boolean bEmailValid = false, bRoleValid= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Get Instance of Database
        dbAdapter = DBAdapter.getInstance(this);
        dbAdapter.open();

        // Instantiate component variables
        edtIdNumber = findViewById(R.id.edtIdNumber);
        edtUsername = findViewById(R.id.edtUserName);
        edtPassword = findViewById(R.id.edtPassword);
        edtEmail = findViewById(R.id.edtEmail);
        radgrpUserRole = findViewById(R.id.radgrpRole);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            // Initialize variables
            String strIdNumber;
            String strUsername = edtUsername.getText().toString();
            String strEmail = edtEmail.getText().toString();
            String strPassword = edtPassword.getText().toString();
            boolean bPasswordValid = isValidPassword(strPassword);
            boolean bUserNameValid = strUsername.isEmpty();

            if (!bRoleValid || !bEmailValid || !bUserNameValid)
            {
                Toast.makeText(RegisterActivity.this, "Please fill all fields correctly!", Toast.LENGTH_SHORT).show();
            } else
            if (!bPasswordValid) {
                Toast.makeText(RegisterActivity.this, "Please use a password at least 8 letters long with at least one symbol, one number, one capital letter and one small letter!", Toast.LENGTH_SHORT).show();
            } else
            if (dbAdapter.getUserId(edtEmail.getText().toString()) == -1)
            {
                Toast.makeText(RegisterActivity.this, "Username already exists!", Toast.LENGTH_SHORT).show();
            } else
             {
                long userId = dbAdapter.insertUser(strUsername, strEmail, strPassword, "Customer");
                if (userId == -1)
                {
                    Toast.makeText(RegisterActivity.this, "User not registered!", Toast.LENGTH_SHORT).show();
                }
                else {
                    dbAdapter.insertNotification(
                            (int)userId,
                            userId + "(" + edtUsername.getText().toString()+ ")"
                                    + "is requesting to be a" + customerRole.toString(),
                            LocalDate.now(ZoneId.systemDefault()).toString(),
                            customerRole.toString());

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    dbAdapter.close();
                }
            }
        });

        radgrpUserRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radConsultant) {
                customerRole = UserRoleEnum.CONSULTANT;
                bRoleValid = true;
            } else if (checkedId == R.id.radCustomer) {
                customerRole = UserRoleEnum.CUSTOMER;
                bRoleValid = true;
            } else if (checkedId == R.id.radAdvisor) {
                customerRole = UserRoleEnum.ADVISOR;
                bRoleValid = true;
            } else {
                bRoleValid = false;
                Toast.makeText(RegisterActivity.this, "Please select a user role!", Toast.LENGTH_SHORT).show();
            }
        });

        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isValidEmail = isValidEmail(s.toString());

                // Update drawable end visibility or color
                if (isValidEmail) {
                    edtEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.done_item, 0);
                    bEmailValid = true;
                } else {
                    edtEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    bEmailValid = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
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

        txtBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            dbAdapter.close();

        });
    }

    public boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false; // Password is too short
        }

        // Check for at least one symbol
        if (!password.matches(".*[!@#$%^&*()_+=\\\\-{}:;'\"<>,.?/|].*")) {
            return false;
        }

        // Check for at least one number
        if (!password.matches(".*\\d.*")) {
            return false;
        }

        // Check for at least one capital letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // Check for at least one small letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        return true; // Password meets all criteria
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


    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close(); // Close the database connection to avoid leaks
    }
}