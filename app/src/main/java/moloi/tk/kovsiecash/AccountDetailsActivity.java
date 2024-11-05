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

import java.time.LocalDate;
import java.time.ZoneId;

public class AccountDetailsActivity extends AppCompatActivity {
    // Declare Variables
    private DBAdapter dbAdapter;
    private EditText edtUsername, edtEmail, edtPassword;
    private Button btnRegister;
    private RadioGroup radgrpUserRole;
    private UserRoleEnum customerRole = UserRoleEnum.Customer;
    int userId;


    boolean bEmailValid = false, bRoleValid= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        // Get Instance of Database
        userId = getIntent().getIntExtra("USER_ID", -1);
        dbAdapter = DBAdapter.getInstance(this);
        dbAdapter.open();

        // Instantiate component variables
        edtUsername = findViewById(R.id.edtUserName);
        edtUsername.setText(dbAdapter.getUserName(userId));
        edtPassword = findViewById(R.id.edtPassword);
        edtPassword.setText(String.valueOf(userId));
        edtEmail = findViewById(R.id.edtEmail);
        edtEmail.setText(dbAdapter.getUserEmail(userId));
        radgrpUserRole = findViewById(R.id.radgrpRole);
        switch (dbAdapter.getUserRole(userId))
        {
            case "Customer": radgrpUserRole.check(R.id.radCustomer); break;
            case "Consultant": radgrpUserRole.check(R.id.radConsultant); break;
            case "Advisor": radgrpUserRole.check(R.id.radAdvisor); break;
        }
        radgrpUserRole.check(R.id.radAdvisor);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            // Initialize variables
            String strUsername = edtUsername.getText().toString();
            edtUsername.setText(strUsername);
            String strEmail = edtEmail.getText().toString();
            edtEmail.setText(strEmail);
            String strPassword = edtPassword.getText().toString();
            boolean bPasswordValid = isValidPassword(strPassword);
            boolean bUserNameValid = !strUsername.isEmpty();

            if (!bEmailValid)
            {
                Toast.makeText(AccountDetailsActivity.this, "Please enter a valid email address!", Toast.LENGTH_SHORT).show();
            } else
            if (!bUserNameValid)
            {
                Toast.makeText(AccountDetailsActivity.this, "Please enter a username!", Toast.LENGTH_SHORT).show();
            } else
            if (!bRoleValid)
            {
                Toast.makeText(AccountDetailsActivity.this, "Please select a role!", Toast.LENGTH_SHORT).show();
            } else
            if (!bPasswordValid) {
                Toast.makeText(AccountDetailsActivity.this, "Please use a password at least 8 letters long with at least one symbol, one number, one capital letter and one small letter!", Toast.LENGTH_SHORT).show();
            } else
            if (dbAdapter.getUserId(edtEmail.getText().toString()) == -1)
            {
                Toast.makeText(AccountDetailsActivity.this, "Username already exists!", Toast.LENGTH_SHORT).show();
            } else
             {
                long check = dbAdapter.updateUserDetails(userId,strUsername, strEmail, strPassword, "Customer");
                if (check == 0)
                {
                    Toast.makeText(AccountDetailsActivity.this, "User not registered!", Toast.LENGTH_SHORT).show();
                }
                else {

                    if (customerRole != UserRoleEnum.Customer)
                    {
                        dbAdapter.insertNotification(
                                1,
                                "User " + userId + "(" + edtUsername.getText().toString()+ ")"
                                        + "is requesting to be a " + customerRole.toString(),
                                LocalDate.now(ZoneId.systemDefault()).toString(),
                                "RoleChange");
                    }
                    dbAdapter.close();
                    finish();
                }
            }
        });

        radgrpUserRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radConsultant) {
                customerRole = UserRoleEnum.Consultant;
                bRoleValid = true;
            } else if (checkedId == R.id.radCustomer) {
                customerRole = UserRoleEnum.Customer;
                bRoleValid = true;
            } else if (checkedId == R.id.radAdvisor) {
                customerRole = UserRoleEnum.Advisor;
                bRoleValid = true;
            } else {
                bRoleValid = false;
                Toast.makeText(AccountDetailsActivity.this, "Please select a user role!", Toast.LENGTH_SHORT).show();
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