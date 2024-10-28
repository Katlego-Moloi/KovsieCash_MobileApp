package moloi.tk.kovsiecash;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.io.IOException;

public class SplashActivity extends AppCompatActivity {
    DBAdapter dbAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
            dbAdapter = DBAdapter.getInstance(this);
            dbAdapter.open();
            try {
                dbAdapter.populateDatabase();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            startActivity(new Intent("moloi.tk.kovsiecash.LoginActivity"));
            finish();
        }, 2000);

    }
}
