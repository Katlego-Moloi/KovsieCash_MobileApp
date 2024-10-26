package moloi.tk.kovsiecash;

import android.os.Bundle;
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

        Spinner accountSpinner = findViewById(R.id.account_spinner);
        List<String> accountNames = dbAdapter.getAccountNames(userId);

        ArrayAdapter<String> accountsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, accountNames);
        accountSpinner.setAdapter(accountsAdapter);

        // Fetch recent transactions
        ArrayList<Transaction> recentTransactions = dbAdapter.getRecentTransactions(userId, 30);

        // Set up RecyclerView
        RecyclerView recyclerView = findViewById(R.id.transaction_recyclerview);

        TransactionAdapter adapter = new TransactionAdapter(this, recentTransactions);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}