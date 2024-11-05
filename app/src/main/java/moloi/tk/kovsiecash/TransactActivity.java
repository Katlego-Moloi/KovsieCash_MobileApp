package moloi.tk.kovsiecash;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
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

public class TransactActivity extends AppCompatActivity {
    // Declare Variables
    private DBAdapter dbAdapter;
    private List<Account> accounts;
    private int userId;
    private String userName;
    private ImageButton btnDashboard, btnAccounts, btnMore, btnReport;
    private String strAccountFrom, strAccountTo;
    private RadioGroup radgrpTransactionType;

    LinearLayout transferLayout;
    LinearLayout paymentLayout;

    EditText edtAmount, edtReference, edtAccountNumberTo ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transact);

        // Get Instance of Database
        dbAdapter = DBAdapter.getInstance(this);
        dbAdapter.open();

        // Get User Data from Database
        userId = getIntent().getIntExtra("USER_ID", -1);
        accounts = dbAdapter.getUserAccounts(userId, 30);
        userName = dbAdapter.getUserName(userId);
        List<String> accountNames = dbAdapter.getAccountNames(userId);

        // Initialise components
        transferLayout = findViewById(R.id.transferLayout);
        paymentLayout = findViewById(R.id.paymentLayout);
        Spinner spnAccountFrom = findViewById(R.id.account_from);
        Spinner spnAccountTo = findViewById(R.id.account_to);
        Button btnTransact = findViewById(R.id.btnTransactDone);
        radgrpTransactionType = findViewById(R.id.radgrpTransactType);
        edtAmount = findViewById(R.id.edtAmount);
        edtReference = findViewById(R.id.edtReference);
        edtAccountNumberTo = findViewById(R.id.edtAccountNumberTo);

        ArrayAdapter<String> accountsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, accountNames);
        spnAccountFrom.setAdapter(accountsAdapter);
        spnAccountTo.setAdapter(accountsAdapter);

        btnTransact.setOnClickListener(v -> {
            double amount = Double.parseDouble(edtAmount.getText().toString());
            String reference = edtReference.getText().toString();

            if (amount <= 0)
            {
                Toast.makeText(TransactActivity.this, "Please enter a valid amount!", Toast.LENGTH_SHORT).show();
            }
            else if (strAccountFrom.isEmpty()) {
                Toast.makeText(TransactActivity.this, "Please select an which account you would like to transfer from!", Toast.LENGTH_SHORT).show();
            }else if (dbAdapter.getAccountByAccountNumber(strAccountFrom, 1).getBalance() < amount )
            {
                Toast.makeText(TransactActivity.this, "Unfortunately you do not have enough funds in that account", Toast.LENGTH_SHORT).show();
            }
            else if (radgrpTransactionType.getCheckedRadioButtonId() == R.id.radTransfer)
            {
                if (strAccountTo.isEmpty())
                {
                    Toast.makeText(TransactActivity.this, "Please select an account to tranfer to!", Toast.LENGTH_SHORT).show();
                } else if (strAccountTo.matches(strAccountFrom))
                {
                    Toast.makeText(TransactActivity.this, "Cannot send to same account!", Toast.LENGTH_SHORT).show();
                }else {
                    dbAdapter.insertTransfer(userId, strAccountFrom, strAccountTo, reference, amount);
                    Toast.makeText(TransactActivity.this, "Transaction Complete", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(TransactActivity.this, MainActivity.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                    finish();                }
            }
            else if (radgrpTransactionType.getCheckedRadioButtonId() == R.id.radPayment)
            {
                strAccountTo = edtAccountNumberTo.getText().toString();
                
                if (strAccountTo.isEmpty())
                {
                    Toast.makeText(TransactActivity.this, "Please enter the account number you would like to pay to!", Toast.LENGTH_SHORT).show();
                }else if (strAccountTo.matches(strAccountFrom))
                {
                    Toast.makeText(TransactActivity.this, "Cannot send to same account!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (dbAdapter.CheckForAccount(strAccountTo))
                    {
                        dbAdapter.insertPayment(userId, dbAdapter.getUserByAccountNumber(strAccountTo), strAccountFrom, strAccountTo, reference, amount);
                        Toast.makeText(TransactActivity.this, "Payment Complete", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(TransactActivity.this, MainActivity.class);
                        intent.putExtra("USER_ID", userId);
                        startActivity(intent);
                        finish();                    }
                    else
                    {
                        Toast.makeText(TransactActivity.this, "Account does not exist!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        spnAccountFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Fetch the selected account and store in variable
                Account selectedAccount = accounts.get(position);
                strAccountFrom = selectedAccount.getAccountNumber();

                // Display Selected Account
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentAccounts = fragmentManager.beginTransaction();

                AccountFragment fragment = AccountFragment.newInstance(selectedAccount, userName);
                fragmentAccounts.replace(R.id.accountFromView, fragment);

                fragmentAccounts.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spnAccountTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Fetch recent transactions
                Account selectedAccount = accounts.get(position);
                strAccountTo = selectedAccount.getAccountNumber();

                // Display Selected Account
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentAccounts = fragmentManager.beginTransaction();

                AccountFragment fragment = AccountFragment.newInstance(selectedAccount, userName);
                fragmentAccounts.replace(R.id.accountToView, fragment);

                fragmentAccounts.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        radgrpTransactionType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radTransfer) {
                // Set Display to match
                transferLayout.setVisibility(View.VISIBLE);
                paymentLayout.setVisibility(View.GONE);

            } else if (checkedId == R.id.radPayment) {
                // Set Display to match
                paymentLayout.setVisibility(View.VISIBLE);
                transferLayout.setVisibility(View.GONE);

            } else {
                Toast.makeText(TransactActivity.this, "Please select a transaction type!", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialise Navigation Bar
        btnDashboard = findViewById(R.id.btnDashboard);
        btnAccounts = findViewById(R.id.btnAccounts);
        btnReport = findViewById(R.id.btnReport);
        btnMore = findViewById(R.id.btnMore);

        // Set navigation bar methods
        btnDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(TransactActivity.this, MainActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            finish();
        });
        btnAccounts.setOnClickListener(v -> {
            Intent intent = new Intent(TransactActivity.this, AccountActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            finish();
        });
//        btnReport.setOnClickListener(v -> {
//            Intent intent = new Intent(TransactActivity.this, ReportActivity.class);
//            startActivity(intent);
//            finish();
//        });
        btnMore.setOnClickListener(v -> {
            Intent intent = new Intent(TransactActivity.this, MoreActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        dbAdapter.close();
        super.onDestroy();

    }

}