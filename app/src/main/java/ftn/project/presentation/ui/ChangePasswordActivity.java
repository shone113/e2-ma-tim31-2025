package ftn.project.presentation.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ftn.project.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etOldPass, etNewPass, etConfirmPass;
    private Button btnReset;
    private TextView tvFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setContentView(R.layout.activity_change_password); // tvoj XML fajl

        etOldPass = findViewById(R.id.etOldPassword);
        etNewPass = findViewById(R.id.etNewPassword);
        etConfirmPass = findViewById(R.id.etConfirmPassword);
        btnReset = findViewById(R.id.btnResetPassword);
        tvFeedback = findViewById(R.id.tvFeedback);

        btnReset.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String oldPass = etOldPass.getText().toString().trim();
        String newPass = etNewPass.getText().toString().trim();
        String confirmPass = etConfirmPass.getText().toString().trim();

        // Reset feedback
        tvFeedback.setText("");

        // Validacija
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            tvFeedback.setText("Popunite sva polja!");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            tvFeedback.setText("Nove lozinke se ne poklapaju!");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvFeedback.setText("Niste prijavljeni!");
            return;
        }

        // Reautentifikacija
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Promena lozinke
                user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        tvFeedback.setText("Lozinka uspešno promenjena!");
                    } else {
                        tvFeedback.setText("Greška: " + updateTask.getException().getMessage());
                    }
                });
            } else {
                tvFeedback.setText("Stara lozinka nije tačna!");
            }
        });
    }
}