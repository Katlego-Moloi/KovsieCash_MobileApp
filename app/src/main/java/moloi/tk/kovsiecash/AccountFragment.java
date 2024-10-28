package moloi.tk.kovsiecash;
import android.content.ClipData;
import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.util.Locale;

public class AccountFragment extends Fragment {

    private String accountNumber;
    private String accountName;
    private String userName; // Added for user's name
    private double balance;

    public static AccountFragment newInstance(Account account, String userName) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putString("accountNumber", account.getAccountNumber());
        args.putString("accountName", account.getAccountName());
        args.putString("userName", userName); // Pass user's name
        args.putDouble("balance", account.getBalance());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Get account data and user's name from arguments
        accountNumber = getArguments().getString("accountNumber");
        accountName = getArguments().getString("accountName");
        userName = getArguments().getString("userName");
        balance = getArguments().getDouble("balance");

        // Set data to views
        TextView accountNameTextView = view.findViewById(R.id.txtAccountName);
        accountNameTextView.setText(accountName);

        TextView accountNumberTextView = view.findViewById(R.id.txtAccountNumber);
        accountNumberTextView.setText(accountNumber);

        TextView userNameTextView = view.findViewById(R.id.txtUsername);
        userNameTextView.setText(userName);

        Button copyAccountNumber = view.findViewById(R.id.copyAccountNumber);

        TextView balanceTextView = view.findViewById(R.id.txtBalance);
        Locale locale = new Locale("en", "ZA");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        String formattedBalance = currencyFormatter.format(balance);
        currencyFormatter.setMinimumFractionDigits(2);
        currencyFormatter.setMaximumFractionDigits(2);
        balanceTextView.setText(formattedBalance);

        copyAccountNumber.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager)(requireContext().getSystemService(Context.CLIPBOARD_SERVICE));
            ClipData clip = ClipData.newPlainText("Account Number", accountNumberTextView.getText().toString());
            clipboard.setPrimaryClip(clip);

            // Optional: Show a Toast message to indicate success
            Toast.makeText(requireContext(), "Account number copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}