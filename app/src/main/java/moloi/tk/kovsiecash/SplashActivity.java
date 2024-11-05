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
        dbAdapter = DBAdapter.getInstance(this);
        dbAdapter.open();

        new Handler().postDelayed(() -> {
            try {
                dbAdapter.populateDatabase();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            startActivity(new Intent("moloi.tk.kovsiecash.LoginActivity"));
            finish();
        }, 2000);

    }

    @Override
    protected void onDestroy() {
        dbAdapter.close();
        super.onDestroy();

    }
}
