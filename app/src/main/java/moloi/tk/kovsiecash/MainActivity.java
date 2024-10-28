package moloi.tk.kovsiecash;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {
    // Declare Variables
    private DBAdapter dbAdapter;
    List<Account> accounts;
    int userId;
    String userName;

    ImageButton btnAccounts, btnTransact, btnMore, btnReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnAccounts = findViewById(R.id.btnAccounts);
        btnTransact = findViewById(R.id.btnTransact);
        btnMore = findViewById(R.id.btnMore);

        // Get Instance of Database
        dbAdapter = DBAdapter.getInstance(this);
        dbAdapter.open();

        LinearLayout accountFragmentContainer = findViewById(R.id.account_fragment_container);

        userId = getIntent().getIntExtra("USER_ID", -1);
        accounts = dbAdapter.getUserAccounts(userId, 30);
        userName = dbAdapter.getUserName(userId);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        for (Account account : accounts) {
            AccountFragment fragment = AccountFragment.newInstance(account, userName);
            fragmentTransaction.add(R.id.account_fragment_container, fragment);
        }

        fragmentTransaction.commit();

        // Fetch recent transactions
        ArrayList<Transaction> recentTransactions = dbAdapter.getRecentTransactions(userId, 10);

        // Set up RecyclerView
        RecyclerView recyclerView = findViewById(R.id.transaction_recyclerview);

        TransactionAdapter adapter = new TransactionAdapter(this, recentTransactions);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnAccounts.setOnClickListener(v -> {
            // Create the intent and add the user ID
            Intent intent = new Intent(MainActivity.this, AccountActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            finish();
        });

        btnTransact.setOnClickListener(v -> {
            // Create the intent and add the user ID
            Intent intent = new Intent(MainActivity.this, TransactActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            finish();
        });

        btnMore.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MoreActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            finish();
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close(); // Close the database connection to avoid leaks
    }
}