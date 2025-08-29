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

import java.time.LocalDateTime;
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
        String username = etUsername.getText().toString().trim();

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
                                // prvo obezbedjujemo lokalnog user-a pa saljemo mejl
                                allocateGlobalIdAndSaveLocalWithCallback(user, username, selectedAvatarResId, () -> {
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(AuthActivity.this, t -> {
                                                if (t.isSuccessful()) {
                                                    Toast.makeText(AuthActivity.this,
                                                            "Poslat je verifikacioni email. Proveri inbox/spam.",
                                                            Toast.LENGTH_LONG).show();
                                                    startVerificationPolling(); // ovde smo sigurni da lokalni User postoji
                                                } else {
                                                    Toast.makeText(AuthActivity.this,
                                                            "Greška pri slanju verifikacije: " +
                                                                    (t.getException() != null ? t.getException().getMessage() : ""),
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            });
                                });

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
    private void ensureLocalUserExists(FirebaseUser fb, String usernameHint, int avatarResId, Runnable onDone) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            User local = db.userRepository().getByFirebaseUid(fb.getUid());
            if (local != null) {
                runOnUiThread(onDone);
                return;
            }
            // Nema lokalnog reda → dodeli/uzmi globalni intId u Firestore i sačuvaj lokalno
            allocateGlobalIdAndSaveLocalWithCallback(fb, usernameHint, avatarResId, onDone);
        });
    }
    // NOVA verzija sa callback-om
    private void allocateGlobalIdAndSaveLocalWithCallback(
            FirebaseUser fb,
            String username,
            int selectedAvatarResId,
            Runnable onDone   // ← (1) DODAT PARAMETAR
    ) {
        if (fb == null) {
            Toast.makeText(this, "Nisi prijavljen.", Toast.LENGTH_SHORT).show();
            // opciono: obavesti pozivaoca da je gotovo (bezuspešno)
            if (onDone != null) runOnUiThread(onDone);   // ← (2) POZIV CALLBACK-a
            return;
        }

        com.google.firebase.firestore.FirebaseFirestore fs =
                com.google.firebase.firestore.FirebaseFirestore.getInstance();
        com.google.firebase.firestore.DocumentReference counterRef =
                fs.collection("counters").document("users");
        com.google.firebase.firestore.DocumentReference userRef =
                fs.collection("users").document(fb.getUid());

        // 1) Ako već postoji intId u Firestore-u → upiši lokalno i pozovi onDone
        userRef.get().addOnSuccessListener(snap -> {
            Long existing = (snap.exists() ? snap.getLong("intId") : null);
            if (existing != null) {
                int intId = existing.intValue();
                saveLocally(intId, fb.getUid(), username, selectedAvatarResId);
                if (onDone != null) runOnUiThread(onDone);   // ← (3) POZIV CALLBACK-a
                return; // ← (4) PREKINI DALJE IZVRŠAVANJE
            }

            // 2) U suprotnom, dodeli novi intId u transakciji
            fs.runTransaction(tr -> {
                com.google.firebase.firestore.DocumentSnapshot c = tr.get(counterRef);
                long next;
                Long cur = (c.exists() ? c.getLong("nextId") : null);
                if (cur == null) {
                    next = 1000L; // start vrednost
                    java.util.Map<String, Object> init = new java.util.HashMap<>();
                    init.put("nextId", next + 1L);
                    tr.set(counterRef, init);
                } else {
                    next = cur;
                    tr.update(counterRef, "nextId", cur + 1L);
                }

                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("intId", next);
                data.put("username", username);
                data.put("createdAt", com.google.firebase.Timestamp.now());
                tr.set(userRef, data, com.google.firebase.firestore.SetOptions.merge());

                return next;
            }).addOnSuccessListener(nextId -> {
                int intId = (int) (long) nextId;
                saveLocally(intId, fb.getUid(), username, selectedAvatarResId);
                if (onDone != null) runOnUiThread(onDone);   // ← (5) POZIV CALLBACK-a
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Neuspešna dodela ID-a: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                // namerno ne zovem onDone ovde; pozivalac može da reši grešku po potrebi
            });

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Greška čitanja korisnika: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            // i ovde ne zovem onDone, jer je to "hard" fail
        });
    }

    private void saveLocally(int userId, String firebaseUid, String username, int selectedAvatarResId) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            User existing = db.userRepository().getByFirebaseUid(firebaseUid);

            if (existing == null) {
                User nu = new User();
                nu.setUserId(userId);                 // ← ključni deo: koristimo globalni int
                nu.setFirebaseUid(firebaseUid);
                nu.setUsername(username);
                // ako čuvaš ime slike, ne resId:
                // nu.setAvatarImage(ImageResId.nameForRes(this, selectedAvatarResId));
                db.userRepository().insert(nu);
            } else {
                // već postoji lokalno (npr. re-instalacija): po želji sync-uj username/avatar
                // existing.setUsername(username);
                // db.userRepository().update(existing);
            }

            runOnUiThread(() ->
                    Toast.makeText(this, "Korisnik sačuvan lokalno (ID: " + userId + ")", Toast.LENGTH_SHORT).show()
            );
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
        u.setEmailVerified(fbUser.isEmailVerified());
        u.setFirebaseUid(fbUser.getUid());
        u.setNewLevelTime(LocalDateTime.now());

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
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    // neočekivano: nema prijavljenog korisnika → prekini polling
                    stopVerificationPolling();
                    Toast.makeText(AuthActivity.this, "Nalog više nije prijavljen.", Toast.LENGTH_SHORT).show();
                    return;
                }

                user.reload().addOnCompleteListener(t -> {
                    if (!t.isSuccessful()) {
                        // mali retry ako reload failuje
                        verifyHandler.postDelayed(verifyTask, POLL_MS);
                        return;
                    }

                    if (user.isEmailVerified()) {
                        // 1) Obeleži verifikovan u lokalnoj bazi
                        // 2) PROVERI da li postoji lokalni User; ako ne — kreiraj ga pre navigacije
                        Executors.newSingleThreadExecutor().execute(() -> {
                            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                            try {
                                db.userRepository().markVerified(user.getUid(), true);
                            } catch (Exception ignored) { }

                            User local = db.userRepository().getByFirebaseUid(user.getUid());

                            runOnUiThread(() -> {
                                // šta radimo kad je sve spremno:
                                Runnable goNext = () -> {
                                    animView.cancelAnimation();
                                    showVerifyUI(false);
                                    startActivity(new Intent(AuthActivity.this, AllUsersActivity.class));
                                    finish();
                                };

                                if (local == null) {
                                    // nema lokalnog reda → napravi ga kroz Firestore counter transakciju,
                                    // pa tek onda idi dalje
                                    String hint = deriveUsername(user); // fallback username
                                    allocateGlobalIdAndSaveLocalWithCallback(
                                            user,
                                            hint,
                                            selectedAvatarResId,
                                            goNext
                                    );
                                } else {
                                    // već postoji lokalni user → može navigacija odmah
                                    goNext.run();
                                }
                            });
                        });
                    } else {
                        // još uvek nije verifikovan → nastavi polling
                        verifyHandler.postDelayed(verifyTask, POLL_MS);
                    }
                });
            }
        };

        verifyHandler.post(verifyTask);
    }

    private String deriveUsername(FirebaseUser fb) {
        if (etUsername != null && etUsername.getVisibility() == View.VISIBLE) {
            String ui = etUsername.getText().toString().trim();
            if (!ui.isEmpty()) return ui;
        }
        String email = fb.getEmail();
        if (email != null && email.contains("@")) return email.substring(0, email.indexOf('@'));
        return "Player";
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