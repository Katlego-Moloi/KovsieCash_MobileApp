package moloi.tk.kovsiecash;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    Context context;
    private ArrayList<Notification> notifications;

    public NotificationAdapter(Context context, ArrayList<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationAdapter.NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        // Set transaction data to views in the fragment
        switch (notification.getType().toLowerCase()){
            case "deposit": holder.imgNotificationType.setImageResource(R.drawable.ic_deposit);
                holder.notificationType.setBackgroundResource(R.drawable.green_circle_soft);break;
            case "withdrawal": holder.imgNotificationType.setImageResource(R.drawable.ic_withdrawal);
                holder.notificationType.setBackgroundResource(R.drawable.red_circle_soft);break;
            case "transfer": holder.imgNotificationType.setImageResource(R.drawable.ic_transfer);
                holder.notificationType.setBackgroundResource(R.drawable.blue_circle_soft);break;
            case "rolechange": holder.imgNotificationType.setImageResource(R.drawable.ic_person_add); holder.
                    roleChangeOptions.setVisibility(View.VISIBLE); break;
            case "advice": holder.imgNotificationType.setImageResource(R.drawable.ic_person_add);
            holder.imgNotificationType.setOnClickListener(v -> {
                // Extract advice ID from notification description
                String adviceIdString = notification.getNotificationDescription().
                        substring(notification.getNotificationDescription().indexOf("- ") + 2);
                int adviceId = Integer.parseInt(adviceIdString);

                // Get advice details using getAdvice(adviceId)
                DBAdapter dbAdapter = DBAdapter.getInstance(context);
                String[] advice = dbAdapter.getAdvice(adviceId); // Assuming you have a dbAdapter instance
                dbAdapter.setNotificationRead(notification.getNotificationID());
                dbAdapter.close();

                // Build the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(advice[0]); // Set dialog title to advice title
                builder.setMessage(advice[1]); // Set dialog message to advice description

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Set notification to read
                        dialog.dismiss();
                    }
                });

                // Show the dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }); break;
            default: break;
        }

        String description = notification.getNotificationDescription();
        holder.txtNotification.setText(String.valueOf(description));
        holder.txtNotificationDate.setText(String.valueOf(notification.getNotificationDateTime()));

        holder.btnApprove.setOnClickListener(v -> {
            int iposition = description.indexOf(" a ");
            String roleRequested = description.substring(iposition + 3);


            iposition = description.indexOf("(");
            String strRequestUserId = description.substring( 5, iposition);
            int requestUserId = Integer.parseInt(strRequestUserId);

            DBAdapter dbAdapter = DBAdapter.getInstance(context);
            dbAdapter.updateUserRole(requestUserId, roleRequested);
            dbAdapter.setNotificationRead(notification.getNotificationID());
            dbAdapter.close();

            Toast.makeText(context, "Role changed to " + roleRequested, Toast.LENGTH_SHORT).show();

        });

        holder.btnDissaprove.setOnClickListener(v -> {
            DBAdapter dbAdapter = DBAdapter.getInstance(context);
            dbAdapter.setNotificationRead(notification.getNotificationID());
            dbAdapter.close();
            Toast.makeText(context, "Request Successfully Denied" , Toast.LENGTH_SHORT).show();

        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgNotificationType;
        LinearLayout roleChangeOptions;
        TextView txtNotification, txtNotificationDate;
        ImageButton btnApprove, btnDissaprove;

        LinearLayout notificationType;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views from the fragment layout
            imgNotificationType = itemView.findViewById(R.id.imgNotificationType);
            notificationType = itemView.findViewById(R.id.notificationType);
            roleChangeOptions = itemView.findViewById(R.id.roleChangeOptions);
            txtNotification = itemView.findViewById(R.id.txtNotification);
            txtNotificationDate = itemView.findViewById(R.id.txtNotificationDate);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnDissaprove = itemView.findViewById(R.id.btnDissaprove);


        }
    }
}