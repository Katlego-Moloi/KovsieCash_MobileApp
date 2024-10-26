package moloi.tk.kovsiecash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

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

        TextView balanceTextView = view.findViewById(R.id.txtBalance);
        balanceTextView.setText("Balance: " + balance);

        return view;
    }
}