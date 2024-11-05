package moloi.tk.kovsiecash;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.util.Log;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MoreActivity extends AppCompatActivity {
    // Declare Variables
    private DBAdapter dbAdapter;
    TextView txtUserName, txtEmail;
    int userId, iNotifications;
    ImageButton btnCollapseNotifications, btnBack;

    LinearLayout noNotisDisplay, notificationCountDisplay, notificationsLayout;
    ArrayList<Notification> arrNotifications;
    RecyclerView notificationsView;
    TextView notificationCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        // Get Instance of Database
        dbAdapter = DBAdapter.getInstance(this);
        dbAdapter.open();
        userId = getIntent().getIntExtra("USER_ID", -1);
        iNotifications = dbAdapter.countUnreadNotis(userId);


        // Initialise Component Variables
        txtUserName = findViewById(R.id.txtUsername);
        txtUserName.setText(dbAdapter.getUserName(userId));
        txtEmail = findViewById(R.id.txtEmail);
        txtEmail.setText(dbAdapter.getUserEmail(userId));
        btnCollapseNotifications = findViewById(R.id.btnCollapseNotifications);
        btnBack = findViewById(R.id.btnBack);
        noNotisDisplay = findViewById(R.id.noNotisDisplay);
        notificationsView = findViewById(R.id.notificationsView);
        notificationCountDisplay = findViewById(R.id.notificationCountDisplay);
        notificationCount = findViewById(R.id.notificationCount);
        notificationsLayout = findViewById(R.id.notificationsLayout);

        if (iNotifications < 1)
        {
            noNotisDisplay.setVisibility(View.VISIBLE);
            notificationsView.setVisibility(View.GONE);
            notificationCountDisplay.setVisibility(View.GONE);

        } else
        {
            // Display notifications
            noNotisDisplay.setVisibility(View.GONE);
            notificationsView.setVisibility(View.VISIBLE);
            notificationCountDisplay.setVisibility(View.VISIBLE);
            notificationCount.setText(String.valueOf(iNotifications));

            // Popoulate Recycler view with notifications
            arrNotifications = dbAdapter.getUnreadNotis(userId);
            RecyclerView recyclerView = findViewById(R.id.notificationsView);
            NotificationAdapter adapter = new NotificationAdapter(this, arrNotifications);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        btnCollapseNotifications.setOnClickListener(v -> {
            if (notificationsLayout.getVisibility() == View.VISIBLE)
            {
                btnCollapseNotifications.setImageResource(R.drawable.ic_arrow_down);
                notificationsLayout.setVisibility(View.GONE);
            }
            else
            {
                btnCollapseNotifications.setImageResource(R.drawable.ic_arrow_up);
                dbAdapter.setUsersNotisRead(userId);
                notificationsLayout.setVisibility(View.VISIBLE);
            }
        });

        ConstraintLayout changeDetailsWidget = findViewById(R.id.changeDetailsWidget);
        changeDetailsWidget.setOnClickListener(v -> {
            Intent intent = new Intent(MoreActivity.this, AccountDetailsActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        ConstraintLayout logOutWidget = findViewById(R.id.logOutWidget);
        logOutWidget.setOnClickListener(v -> {
            deleteRememberMeFile();
            Intent intent = new Intent(MoreActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    public void deleteRememberMeFile() {
        File file = new File(this.getFilesDir(), "rememberme.txt");

        if (file.exists()) {
            boolean deleted = file.delete();

            if (deleted) {
                Toast.makeText(this, "Your profile will no longer be remembered", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete remember me file.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Successfully Logged Out", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        dbAdapter.close();
        super.onDestroy();

    }
}