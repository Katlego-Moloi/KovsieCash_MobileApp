package moloi.tk.kovsiecash;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends AppCompatActivity {
    // Declare Variables
    private DBAdapter dbAdapter;
    List<Account> accounts;
    int userId;
    String userName;

    ImageButton btnTransact, btnDashboard, btnMore, btnReport;

    ImageButton btnAccounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get Instance of Database
        dbAdapter = DBAdapter.getInstance(this);
        dbAdapter.open();

        userId = getIntent().getIntExtra("USER_ID", -1);
        accounts = dbAdapter.getUserAccounts(userId, 30);
        userName = dbAdapter.getUserName(userId);

        // Initialise Componenets
        btnDashboard = findViewById(R.id.btnDashboard);
        btnTransact = findViewById(R.id.btnTransact);
        btnReport = findViewById(R.id.btnReport);
        btnMore = findViewById(R.id.btnMore);

        Spinner accountSpinner = findViewById(R.id.account_spinner);
        List<String> accountNames = dbAdapter.getAccountNames(userId);

        ArrayAdapter<String> accountsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, accountNames);
        accountSpinner.setAdapter(accountsAdapter);

        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Fetch recent transactions
                ArrayList<Transaction> accountTransactions = (ArrayList<Transaction>) accounts.get(position).getTransactions();

                // Set up RecyclerView
                RecyclerView recyclerView = findViewById(R.id.transaction_recyclerview);

                TransactionAdapter adapter = new TransactionAdapter(AccountActivity.this, accountTransactions);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(AccountActivity.this));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnTransact.setOnClickListener(v -> {
            // Create the intent and add the user ID
            Intent intent = new Intent(AccountActivity.this, TransactActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            finish();
        });

        btnDashboard.setOnClickListener(v -> {
            // Create the intent and add the user ID
            Intent intent = new Intent(AccountActivity.this, MainActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            finish();
        });

        btnMore.setOnClickListener(v -> {
            // Create the intent and add the user ID
            Intent intent = new Intent(AccountActivity.this, MoreActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            finish();
        });

    }

}