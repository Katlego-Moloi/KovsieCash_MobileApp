package moloi.tk.kovsiecash;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.TextUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AdviceActivity extends AppCompatActivity {
    // Declare Variables
    private DBAdapter dbAdapter;
    private EditText edtAdvice, edtTitle;
    private Button btnSubmit;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advice);

        // Get Instance of Database
        dbAdapter = DBAdapter.getInstance(this);
        dbAdapter.open();
        int adviserId = getIntent().getIntExtra("ADVISER_ID", -1);
        int adviseeId = getIntent().getIntExtra("ADVISEE_ID", -1);

        // Instantiate component variables
        edtAdvice = findViewById(R.id.edtAdvice);
        edtTitle = findViewById(R.id.edtTitle);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            // Initialize variables
            String strAdvice = edtAdvice.getText().toString();
            String strTitle = edtTitle.getText().toString();

            if (strTitle.isBlank())
            {
                Toast.makeText(AdviceActivity.this, "Please enter a valid title!", Toast.LENGTH_SHORT).show();
            } else
            if (strAdvice.isBlank())
            {
                Toast.makeText(AdviceActivity.this, "Please enter valid advice!", Toast.LENGTH_SHORT).show();
            } else
             {
                 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                 dbAdapter.insertAdvice(strTitle, strAdvice,LocalDate.now(ZoneId.systemDefault()).format(formatter),  adviserId, adviseeId);
                 Toast.makeText(AdviceActivity.this, "Thank you for your advice!", Toast.LENGTH_SHORT).show();
                 finish();

             }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close(); // Close the database connection to avoid leaks
    }
}