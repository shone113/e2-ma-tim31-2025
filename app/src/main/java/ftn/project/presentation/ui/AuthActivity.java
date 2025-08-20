package ftn.project.presentation.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ftn.project.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";
    // [START declare_auth]
    private FirebaseAuth mAuth;

    private ToggleButton toggleAuth;
    private EditText etEmail, etPass, etConfirm, etUsername;
    private Spinner spAvatar;
    private Button btnSubmit;

    private boolean isRegisterMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        toggleAuth = findViewById(R.id.toggleAuth);
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);
        etConfirm = findViewById(R.id.etConfirm);
        etUsername = findViewById(R.id.etUsername);
        spAvatar = findViewById(R.id.spAvatar);
        btnSubmit = findViewById(R.id.btnSubmit);

//        if (savedInstanceState == null) {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.host, new LoginFragment())
//                    .commit();
//        }
        toggleAuth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isRegisterMode = isChecked;
            if (isRegisterMode) {
                etConfirm.setVisibility(View.VISIBLE);
                etUsername.setVisibility(View.VISIBLE);
                spAvatar.setVisibility(View.VISIBLE);
                btnSubmit.setText("Registruj se");
            } else {
                etConfirm.setVisibility(View.GONE);
                etUsername.setVisibility(View.GONE);
                spAvatar.setVisibility(View.GONE);
                btnSubmit.setText("Prijavi se");
            }
        });

        btnSubmit.setOnClickListener(v -> {
            if (isRegisterMode) {
                doRegister();
            } else {
                doLogin();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //
        }
    }

    private void doRegister(){
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString();
        String confirm = etConfirm.getText().toString();

        if (email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Popunite sva polja", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Lozinke se ne poklapaju", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(AuthActivity.this, new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> t) {
                                                if (t.isSuccessful()) {
                                                    Toast.makeText(AuthActivity.this,
                                                            "Poslat je verifikacioni email. Proveri inbox/spam.",
                                                            Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(AuthActivity.this,
                                                            "Greška pri slanju verifikacije: " +
                                                                    (t.getException() != null ? t.getException().getMessage() : ""),
                                                            Toast.LENGTH_LONG).show();
                                                }
                                                mAuth.signOut();
                                            }
                                        });
                            }
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(AuthActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void doLogin(){
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Unesite email i lozinku", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user == null) { Toast.makeText(AuthActivity.this, "Neuspešna prijava.", Toast.LENGTH_SHORT).show(); return; }

                            user.reload().addOnCompleteListener(r -> {
                                if (user.isEmailVerified()) {
                                    Toast.makeText(AuthActivity.this, "Uspešna prijava", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(AuthActivity.this, AllUsersActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(AuthActivity.this, "Nalog nije verifikovan. Proveri email.", Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(AuthActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void sendEmailVerification() {
        // Send verification email
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Email sent
                    }
                });
    }

}