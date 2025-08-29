package ftn.project.presentation.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import ftn.project.R;
import ftn.project.data.database.FirestoreSync;
import ftn.project.data.db.AppDatabase;
import ftn.project.data.dto.UserFriendDTO;
import ftn.project.data.repository.FriendshipRepository;
import ftn.project.domain.entity.Friendship;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.FriendshipService;
import ftn.project.presentation.adapter.UserAdapter;

public class AllUsersActivity extends AppCompatActivity {

    private UserAdapter adapter;
    private ArrayList<UserFriendDTO> friendDTOs;
    private FriendshipRepository friendshipRepository;
    private FriendshipService friendshipService;
    private AppDatabase db;
    private User loggedUser;
    private ActivityResultLauncher<ScanOptions> barcodeLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_users);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = AppDatabase.getInstance(getApplicationContext());
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        friendshipService = new FriendshipService();

        FirestoreSync.syncAllUsersDown(
                getApplicationContext(),
                db,
                () -> Toast.makeText(this, "Users synced ✔", Toast.LENGTH_SHORT).show()
        );
        FirestoreSync.syncFriendshipsDown(
                getApplicationContext(),
                db,
                firebaseUser.getUid(),
                () -> Toast.makeText(this, "Friendships synced ✔", Toast.LENGTH_SHORT).show()
        );

        ListView lvUsers = findViewById(R.id.lvUsers);
        EditText etSearch = findViewById(R.id.etSearch);
        Button btnSearch = findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(v -> performSearch());

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() == null) {
                // otkazano
                return;
            }
            handleScannedQr(result.getContents());
        });

        findViewById(R.id.btnScanQr).setOnClickListener(v -> {
            ScanOptions options = new ScanOptions()
                    .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    .setPrompt("Skeniraj HabitQuest QR prijatelja")
                    .setBeepEnabled(true)
                    .setOrientationLocked(true);
            barcodeLauncher.launch(options);
        });

        loggedUser = db.userRepository().getByFirebaseUid(firebaseUser.getUid());
        ArrayList<Friendship> friendships = new ArrayList<>(db.friendshipRepository().getAllForUserId(loggedUser.getUserId()));
        List<User> users = db.userRepository().getAll();
        friendDTOs = friendshipService.getFriendsForUser(friendships, users, loggedUser.getUserId());

        adapter = new UserAdapter(this, friendDTOs, userFriendDTO -> {
            Friendship friendship = new Friendship();
            friendship.setFirstUserId(loggedUser.getUserId());
            friendship.setSecondUserId(userFriendDTO.userId);
            db.friendshipRepository().insert(friendship);

            User friend = db.userRepository().getById(friendship.secondUserId);
            FirestoreSync.mirrorFriendshipToFirestore(
                    getApplicationContext(),
                    firebaseUser.getUid(),
                    friendship.firstUserId,
                    friend.getFirebaseUid(),
                    friendship.getSecondUserId());
        });

        lvUsers.setAdapter(adapter);
    }

    private void performSearch() {
        String q = ((EditText) findViewById(R.id.etSearch))
                .getText().toString().trim();
        if (q.length() < 2) {
            Toast.makeText(this, "Unesi bar 2 slova", Toast.LENGTH_SHORT).show();
            return;
        }
        List<UserFriendDTO> users = db.userRepository().searchNonFriendUsersWithFlag(q, loggedUser.getUserId());
        users.addAll(friendDTOs);

        adapter.replaceAll(users);
        users.clear();
    }

    private void handleScannedQr(String contents) {
        try {
            JSONObject obj = new JSONObject(contents);
            if (!"HabitQuest".equals(obj.optString("app")) ||
                    !"add_friend".equals(obj.optString("type"))) {
                toast("Ovo nije HabitQuest QR.");
                return;
            }

            String scannedUid = obj.getString("uid");
            String scannedUsername = obj.optString("username", "Friend");

            AppDatabase db = AppDatabase.getInstance(this);

            Executors.newSingleThreadExecutor().execute(() -> {
                // 1) Trenutni korisnik
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null) {
                    runOnUiThread(() -> toast("Nisi prijavljen."));
                    return;
                }
                User me = db.userRepository().getByFirebaseUid(firebaseUser.getUid());
                if (me == null) {
                    runOnUiThread(() -> toast("Tvoj lokalni nalog nije pronađen."));
                    return;
                }

                // 2) Ne možeš dodati samog sebe
                if (firebaseUser.getUid().equals(scannedUid)) {
                    runOnUiThread(() -> toast("Ne možeš dodati samog sebe."));
                    return;
                }

                // 3) Pronađi ili kreiraj drugog korisnika po UID-u
                User other = db.userRepository().getByFirebaseUid(scannedUid);
                if (other == null) {
                    other = new User();
                    other.setFirebaseUid(scannedUid);
                    other.setUsername(scannedUsername); // best-effort; kasnije može da se izmeni
                    long id = db.userRepository().insert(other);
                    if (id == -1) { // već postoji iz nekog razloga
                        other = db.userRepository().getByFirebaseUid(scannedUid);
                    } else {
                        other.setUserId((int) id);
                    }
                }

                // 4) Proveri da li već postoji prijateljstvo
                boolean exists = db.friendshipRepository().existsBetween(me.getUserId(), other.getUserId());
                if (exists) {
                    runOnUiThread(() -> toast("Već ste prijatelji."));
                    return;
                }

                // 5) Upis prijateljstva (ovde odmah ACCEPTED, čisto lokalno)
                Friendship f = new Friendship();
                f.setFirstUserId(me.getUserId());
                f.setSecondUserId(other.getUserId());

                db.friendshipRepository().insert(f);

                FirestoreSync.mirrorFriendshipToFirestore(
                        getApplicationContext(),
                        firebaseUser.getUid(),
                        me.getUserId(),
                        other.getFirebaseUid(),
                        other.getUserId()
                );
                runOnUiThread(() -> {
                    //toast("Dodat prijatelj: " + other.getUsername());
                    // osveži listu, adapter.submitList(...) itd. po potrebi
                });
            });

        } catch (JSONException e) {
            toast("Nevažeći QR format.");
        }
    }

    private void toast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

}