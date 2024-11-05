package moloi.tk.kovsiecash;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminAccountActivity extends AppCompatActivity {
    // Declare Variables
    private DBAdapter dbAdapter;
    List<Account> accounts;
    int userId;
    String AccountNumber;
    String userName;

    ImageButton btnTransact, btnDashboard, btnMore, btnReport;

    ImageButton btnAccounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get Instance of Database
        dbAdapter = DBAdapter.getInstance(this);
        dbAdapter.open();

        AccountNumber = getIntent().getStringExtra("ACCOUNT_NUMBER");
        accounts = dbAdapter.getUserAccounts(userId, 30);

        // Initialise Componenets
        TextView txtAccountNumber = findViewById(R.id.txtAccountNumber);
        txtAccountNumber.setText(AccountNumber);

        Account account = dbAdapter.getAccountByAccountNumber(AccountNumber, 30);
        ArrayList<Transaction> accountTransactions = (ArrayList<Transaction>) account.getTransactions();

        // Set up RecyclerView
        RecyclerView recyclerView = findViewById(R.id.transaction_recyclerview);

        TransactionAdapter adapter = new TransactionAdapter(AdminAccountActivity.this, accountTransactions);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(AdminAccountActivity.this));

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            dbAdapter.close();
            finish();
        });

    }

    @Override
    protected void onDestroy() {
        dbAdapter.close();
        super.onDestroy();

    }

}