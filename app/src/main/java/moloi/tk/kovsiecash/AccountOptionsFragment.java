package moloi.tk.kovsiecash;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.util.Locale;

public class AccountOptionsFragment extends Fragment {

    private String accountNumber;
    private String accountName;
    private String userName; // Added for user's name
    private double balance;

    public static AccountOptionsFragment newInstance(Account account) {
        AccountOptionsFragment fragment = new AccountOptionsFragment();
        Bundle args = new Bundle();
        args.putString("accountNumber", account.getAccountNumber());
        args.putString("accountName", account.getAccountName());
        args.putDouble("balance", account.getBalance());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_options, container, false);

        // Get account data and user's name from arguments
        accountNumber = getArguments().getString("accountNumber");
        accountName = getArguments().getString("accountName");
        balance = getArguments().getDouble("balance");

        // Set data to views
        TextView accountNameTextView = view.findViewById(R.id.txtAccountName);
        accountNameTextView.setText(accountName);

        TextView accountNumberTextView = view.findViewById(R.id.txtAccountNumber);
        accountNumberTextView.setText(accountNumber);

        TextView balanceTextView = view.findViewById(R.id.txtBalance);
        Locale locale = new Locale("en", "ZA");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        String formattedBalance = currencyFormatter.format(balance);
        currencyFormatter.setMinimumFractionDigits(2);
        currencyFormatter.setMaximumFractionDigits(2);
        balanceTextView.setText(formattedBalance);

        ImageButton btnInfo = view.findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AdminAccountActivity.class);
            intent.putExtra("ACCOUNT_NUMBER", accountNumber);

            startActivity(intent);
        });

        return view;
    }
}