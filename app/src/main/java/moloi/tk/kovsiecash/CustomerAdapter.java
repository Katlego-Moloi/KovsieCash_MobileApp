package moloi.tk.kovsiecash;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {
    Context context;
    private ArrayList<Customer> customers;

    boolean Admin = false;
    int AdminID;

    public CustomerAdapter(Context context, ArrayList<Customer> customers, boolean admin, int adminID) {
        this.context = context;
        this.customers = customers;
        this.Admin = admin;
        this.AdminID = adminID;
    }

    @NonNull
    @Override
    public CustomerAdapter.CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customers.get(position);

        // Set user data to views in the fragment
        holder.txtUsername.setText(customer.getUsername());
        holder.txtEmail.setText(customer.getEmail());
        ArrayList<Account> customerAccounts = customer.getAccounts();

        // Set the functions of the buttons
        if (!Admin) {
            holder.adminControls.setVisibility(View.GONE);
            holder.btnAdvise.setOnClickListener(v -> {
                Intent intent = new Intent(context, AdviceActivity.class);
                intent.putExtra("ADVISER_ID", AdminID);
                intent.putExtra("ADVISEE_ID", customer.getUserId());
                startActivity(context, intent, null);
            });
        }
        else
        {
            holder.advisorControls.setVisibility(View.GONE);
            holder.btnAddAccount.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Name the new account:");

                final EditText input = new EditText(context);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newAccountName = input.getText().toString();

                        DBAdapter dbAdapter = DBAdapter.getInstance(context);
                        dbAdapter.open();
                        dbAdapter.insertAccountByUserId(customer.getUserId(), newAccountName);
                        Toast.makeText(context, "Account " + newAccountName + " succesfully generated", Toast.LENGTH_SHORT).show();
                        dbAdapter.close();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            });
            holder.btnEditUser.setOnClickListener(v -> {

                Intent intent = new Intent(context, AccountDetailsActivity.class);
                intent.putExtra("USER_ID", customer.getUserId());
                startActivity(context, intent, null);
            });

            holder.btnRemoveUser.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Please type \"delete account\" to delete this user account:");

                final EditText input = new EditText(context);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String response = input.getText().toString();

                        DBAdapter dbAdapter = DBAdapter.getInstance(context);
                        dbAdapter.open();
                        if (response.matches("delete account")){
                            dbAdapter.deleteUserById(customer.getUserId());
                            Toast.makeText(context, "Customer succesfully deleted", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(context, "Account not deleted", Toast.LENGTH_SHORT).show();

                        dbAdapter.close();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            });
        }

        // Create a new container for the current customer's accounts
        LinearLayout customerAccountsContainer = new LinearLayout(context);
        customerAccountsContainer.setOrientation(LinearLayout.VERTICAL);
        int uniqueContainerId = ViewCompat.generateViewId(); // Generate a unique ID
        customerAccountsContainer.setId(uniqueContainerId);

        // Add the container to your customer's layout (e.g., below the customer's name)
        holder.accountsLayout.addView(customerAccountsContainer);

        // Populate the accounts of the User
        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        for (Account account : customerAccounts) {
            AccountOptionsFragment fragment = AccountOptionsFragment.newInstance(account);
            fragmentTransaction.add(uniqueContainerId, fragment);
        }

        fragmentTransaction.commit();
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        public TextView txtUsername, txtEmail;
        public ImageButton btnAddAccount, btnEditUser, btnRemoveUser, btnAdvise;

        public LinearLayout accountsLayout, advisorControls, adminControls;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views from the fragment layout
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            btnAddAccount = itemView.findViewById(R.id.btnAddAccount);
            btnEditUser = itemView.findViewById(R.id.btnEditUser);
            btnRemoveUser = itemView.findViewById(R.id.btnRemoveUser);
            btnAdvise = itemView.findViewById(R.id.btnAdvise);
            accountsLayout = itemView.findViewById(R.id.accountsLayout);
            advisorControls = itemView.findViewById(R.id.advisorControls);
            adminControls = itemView.findViewById(R.id.adminControls);

        }
    }
}