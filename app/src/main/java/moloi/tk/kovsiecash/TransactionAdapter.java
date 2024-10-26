package moloi.tk.kovsiecash;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    Context context;
    private ArrayList<Transaction> transactions;

    public TransactionAdapter(Context context, ArrayList<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionAdapter.TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_transaction, parent, false); // Use item_transaction.xml here
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        // Set transaction data to views in the fragment
        holder.dateTextView.setText(transaction.getDateTime()); // Assuming "DateTime" is the field in your Transaction class
        holder.referenceTextView.setText(transaction.getReference());
        holder.amountTextView.setText(String.valueOf(transaction.getAmount()));
        holder.balanceTextView.setText(String.valueOf(transaction.getBalance()));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView, referenceTextView, amountTextView, balanceTextView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views from the fragment layout
            dateTextView = itemView.findViewById(R.id.transaction_date);
            referenceTextView = itemView.findViewById(R.id.transaction_reference);
            amountTextView = itemView.findViewById(R.id.transaction_amount);
            balanceTextView = itemView.findViewById(R.id.transaction_balance);
        }
    }
}