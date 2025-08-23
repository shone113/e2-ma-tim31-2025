package ftn.project.presentation.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.User;
import ftn.project.presentation.adapter.AvatarAdapter;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";
    // [START declare_auth]
    private FirebaseAuth mAuth;

    private SwitchMaterial switchAuth;
    private EditText etEmail, etPass, etConfirm, etUsername;
    private ViewPager2 avatarPager;
    private CardView avatarCard;
    private int selectedAvatarResId = R.drawable.avatar_1;

    private Button btnSubmit;

    private boolean isRegisterMode = false;

    private LottieAnimationView animView;
    private Handler verifyHandler = new Handler();
    private Runnable verifyTask;
    private static final long POLL_MS = 4000;
    private TextView tvLogin, tvRegister;


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

        animView = findViewById(R.id.animView);
        switchAuth = findViewById(R.id.switchAuth);
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);
        etConfirm = findViewById(R.id.etConfirm);
        etUsername = findViewById(R.id.etUsername);
        avatarPager = findViewById(R.id.avatarPager);
        avatarCard = findViewById(R.id.avatarCard);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvLogin = findViewById(R.id.tvLogin);
        tvRegister = findViewById(R.id.tvRegister);
        setLoginActive();

        ArrayList<Integer> avatars = new ArrayList<>();
        avatars.add(R.drawable.avatar_1);
        avatars.add(R.drawable.avatar_2);
        avatars.add(R.drawable.avatar_3);
        avatars.add(R.drawable.avatar_4);
        avatars.add(R.drawable.avatar_5);

        avatarPager.setAdapter(new AvatarAdapter(avatars));

        avatarPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                super.onPageSelected(position);
                selectedAvatarResId = ((AvatarAdapter) avatarPager.getAdapter()).getItem(position);
            }
        });

        switchAuth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Register mode
                etConfirm.setVisibility(View.VISIBLE);
                etUsername.setVisibility(View.VISIBLE);
                avatarPager.setVisibility(View.VISIBLE);
                avatarCard.setVisibility(View.VISIBLE);
                btnSubmit.setText("Registracija");
                setRegisterActive();
                isRegisterMode = true;
            } else {
                animView.cancelAnimation();
                showVerifyUI(false);
                // Login mode
                etConfirm.setVisibility(View.GONE);
                etUsername.setVisibility(View.GONE);
                avatarPager.setVisibility(View.GONE);
                avatarCard.setVisibility(View.GONE);
                btnSubmit.setText("Prijava");
                setLoginActive();
                isRegisterMode = false;
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

    private void setLoginActive() {
        tvLogin.setTypeface(null, Typeface.BOLD);
        tvLogin.setTextColor(ContextCompat.getColor(this, R.color.blue_soft));

        tvRegister.setTypeface(null, Typeface.NORMAL);
        tvRegister.setTextColor(ContextCompat.getColor(this, android.R.color.black));
    }

    private void setRegisterActive() {
        tvRegister.setTypeface(null, Typeface.BOLD);
        tvRegister.setTextColor(ContextCompat.getColor(this, R.color.blue_soft));

        tvLogin.setTypeface(null, Typeface.NORMAL);
        tvLogin.setTextColor(ContextCompat.getColor(this, android.R.color.black));
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
                                createLocalUser(user, etUsername.getText().toString().trim(), selectedAvatarResId);

                                user.sendEmailVerification()
                                        .addOnCompleteListener(AuthActivity.this, new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> t) {
                                                if (t.isSuccessful()) {
                                                    Toast.makeText(AuthActivity.this,
                                                            "Poslat je verifikacioni email. Proveri inbox/spam.",
                                                            Toast.LENGTH_LONG).show();
                                                            startVerificationPolling();
                                                } else {
                                                    Toast.makeText(AuthActivity.this,
                                                            "Greška pri slanju verifikacije: " +
                                                                    (t.getException() != null ? t.getException().getMessage() : ""),
                                                            Toast.LENGTH_LONG).show();
                                                }
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
    private void createLocalUser(FirebaseUser fbUser, String username, int avatarResId) {
        if (fbUser == null) return;

        // pretvorimo npr. R.drawable.avatar_3 -> "avatar_3" (lakše za čuvanje/učitavanje)
        String avatarName = getResources().getResourceEntryName(avatarResId);

        User u = new User();
        u.setUsername(username);
        u.setEmail(fbUser.getEmail());
        u.setAvatarImage(avatarName);
        u.setEmailVerified(fbUser.isEmailVerified()); // verovatno false odmah posle registracije

        // ubaci u Room na background thread-u
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.userRepository().insert(u);
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

    private void showVerifyUI(boolean show) {
        animView.setVisibility(show ? View.VISIBLE : View.GONE);

        // sakrij/disable polja dok čekamo verifikaciju
        etEmail.setEnabled(!show);
        etPass.setEnabled(!show);
        etConfirm.setEnabled(!show);
        etUsername.setEnabled(!show);
        avatarPager.setEnabled(!show);
        avatarCard.setEnabled(!show);
        btnSubmit.setEnabled(!show);


    }

    private void startVerificationPolling() {
        showVerifyUI(true);
        animView.playAnimation();

        verifyTask = new Runnable() {
            @Override public void run() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    // neočekivano: nema usera – prekini čekanje
                    showVerifyUI(false);
                    return;
                }
                user.reload().addOnCompleteListener(t -> {
                    if (user.isEmailVerified()) {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                            db.userRepository().markVerified(user.getUid(), true);
                        });

                        animView.cancelAnimation();
                        showVerifyUI(false);
                        startActivity(new Intent(AuthActivity.this, AllUsersActivity.class));
                        finish();
                    } else {
                        // nastavi da čekaš
                        verifyHandler.postDelayed(verifyTask, POLL_MS);
                    }
                });
            }
        };
        verifyHandler.post(verifyTask);
    }

    private void stopVerificationPolling() {
        verifyHandler.removeCallbacksAndMessages(null);
        animView.cancelAnimation();
        showVerifyUI(false);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        stopVerificationPolling();
    }

}