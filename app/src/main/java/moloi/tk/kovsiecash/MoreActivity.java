package moloi.tk.kovsiecash;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MoreActivity extends AppCompatActivity {
    // Declare Variables
    private DBAdapter dbAdapter;
    TextView txtUserName, txtEmail;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        // Get Instance of Database
        dbAdapter = DBAdapter.getInstance(this);
        dbAdapter.open();

        userId = getIntent().getIntExtra("USER_ID", -1);

        // Initialise Component Variables
        txtUserName = findViewById(R.id.txtUsername);
        txtUserName.setText(dbAdapter.getUserName(userId));
        txtEmail = findViewById(R.id.txtEmail);
        txtUserName.setText(dbAdapter.getUserEmail(userId));



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close(); // Close the database connection to avoid leaks
    }
}