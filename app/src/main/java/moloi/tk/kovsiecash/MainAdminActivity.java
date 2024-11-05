package moloi.tk.kovsiecash;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class MainAdminActivity extends AppCompatActivity {
    // Declare Variables
    private DBAdapter dbAdapter;
    List<Account> accounts;
    int userId;
    String userName;
    LinearLayout notificationDisplay;
    TextView notificationCount;

    String adminRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Component Variables
        notificationDisplay = findViewById(R.id.notificationDisplay);
        notificationCount = findViewById(R.id.notificationCount);

        // Get Instance of Database
        dbAdapter = DBAdapter.getInstance(this);
        dbAdapter.open();
        userId = getIntent().getIntExtra("USER_ID", -1);
        adminRole = getIntent().getStringExtra("USER_ROLE");

        int iUnreadNotifications = dbAdapter.countUnreadNotis(userId);

        // Set up notifications
        if (iUnreadNotifications < 1)
        {
            notificationDisplay.setVisibility(View.GONE);
            notificationDisplay.setVisibility(View.GONE);

        } else
        {
            notificationCount.setText(String.valueOf(iUnreadNotifications));

        }

        userName = dbAdapter.getUserName(userId);

        // Fetch all customers
        ArrayList<Customer> customers = dbAdapter.getAllCustomers();

        // Set up RecyclerView
        RecyclerView recyclerView = findViewById(R.id.customersView);

        CustomerAdapter adapter = new CustomerAdapter(this, customers,
                !adminRole.matches("Advisor"), userId);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ShapeableImageView imgProfilePicture = findViewById(R.id.imgProfilePicture);
        imgProfilePicture.setOnClickListener(v -> {
            Intent intent = new Intent(MainAdminActivity.this, MoreActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        notificationDisplay.setOnClickListener(v -> {
            Intent intent = new Intent(MainAdminActivity.this, MoreActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close();
    }
}