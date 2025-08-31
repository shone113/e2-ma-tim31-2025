package ftn.project.presentation.ui;

import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import ftn.project.R;
import ftn.project.data.database.FirestoreSync;
import ftn.project.data.db.AppDatabase;
import ftn.project.data.dto.UserFriendDTO;
import ftn.project.domain.entity.Alliance;
import ftn.project.domain.entity.AllianceInvitation;
import ftn.project.domain.entity.Friendship;
import ftn.project.domain.entity.InvitationStatus;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.FriendshipService;
import ftn.project.presentation.adapter.FriendAdapter;
import ftn.project.presentation.adapter.UserAdapter;
import ftn.project.presentation.notification.AllianceInvitationReceiver;
import ftn.project.presentation.notification.Notifier;

public class AllianceActivity extends AppCompatActivity {
    private FriendAdapter adapter;
    private ArrayList<UserFriendDTO> friendDTOs;
    private FriendshipService friendshipService;
    private AppDatabase db;
    private User loggedUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alliance);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = AppDatabase.getInstance(getApplicationContext());
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        loggedUser = db.userRepository().getByFirebaseUid(firebaseUser.getUid());
        friendshipService = new FriendshipService();

        MaterialButton btnCreate = findViewById(R.id.btnCreateAlliance);
        MaterialButton btnDisband = findViewById(R.id.btnDisbandAlliance);
        TextInputEditText etName = findViewById(R.id.etAllianceName);

        Alliance alliance = new Alliance();
        btnCreate.setOnClickListener(v -> {
            etName.setEnabled(false);
            btnCreate.setVisibility(View.GONE);
            btnDisband.setVisibility(View.VISIBLE);

            alliance.setName(etName.getText().toString());
            alliance.setLeaderUserId(loggedUser.getUserId());

            int newAllianceId = (int)db.allianceRepository().insert(alliance);
            if(newAllianceId <= 0){
                btnCreate.setVisibility(View.VISIBLE);
                btnDisband.setVisibility(View.GONE);
                etName.setEnabled(true);
                Log.e("AllianceActivity","Alliance insert failed");
                return;
            }
            alliance.setAllianceId(newAllianceId);
            showFriends(newAllianceId, alliance.getName());
        });

        btnDisband.setOnClickListener(v -> {
            etName.setEnabled(false);
            btnDisband.setVisibility(View.VISIBLE);
            btnDisband.setEnabled(false);
        });
    }
    public void showFriends(int allianceId, String allianceName){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        ArrayList<Friendship> friendships = new ArrayList<>(db.friendshipRepository().getAllForUserId(loggedUser.getUserId()));
        List<User> users = db.userRepository().getAll();
        List<AllianceInvitation> allianceInvitations = db.allianceInvitationRepository().findAllByInviter(loggedUser.getUserId());
        friendDTOs = friendshipService.getFriendsWithInvitationForUser(friendships, users, loggedUser.getUserId(), allianceInvitations);

        adapter = new FriendAdapter(this, friendDTOs, userFriendDTO -> {
            Log.w("ISPIS", "IZVRSIO SAM SE");
            Friendship friendship = new Friendship();
            friendship.setFirstUserId(loggedUser.getUserId());
            friendship.setSecondUserId(userFriendDTO.userId);
            long rowId = db.friendshipRepository().insert(friendship);
            Log.w("UNET_RED", " " + rowId);
            Log.w("SVI_REDOVI", " " + db.allianceInvitationRepository().findAllByInviter(loggedUser.getUserId()).stream().count());
            User friend = db.userRepository().getById(friendship.secondUserId);

            sendInvite(
                    friend.getFirebaseUid(),
                    userFriendDTO.userId,
                    allianceId,
                    allianceName,
                    loggedUser.getUsername(),
                    loggedUser.getUserId()
            );

            FirestoreSync.mirrorFriendshipToFirestore(
                    getApplicationContext(),
                    firebaseUser.getUid(),
                    friendship.firstUserId,
                    friend.getFirebaseUid(),
                    friendship.getSecondUserId());
        });
        ListView lvFriends = findViewById(R.id.lvFriends);
        if (lvFriends == null) {
            Log.e("AllianceActivity","lvFriends not found in layout activity_alliance.xml");
            return;
        }
        lvFriends.setAdapter(adapter);
    }
    private void sendInvite(
            String inviteeFirebaseUid,
            int inviteeUserId,
            int allianceId,
            String allianceName,
            String inviterName,
            int inviterUserId
    ) {
        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        String docId = fs.collection("allianceInvites").document().getId();
        FirebaseFirestore fdb = FirebaseFirestore.getInstance();

        String inviterUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> inv = new HashMap<>();
        inv.put("allianceId",       allianceId);
        inv.put("allianceName",     allianceName);
        inv.put("inviterUid",       inviterUid);          // string (Firebase UID)
        inv.put("inviteeUid",       inviteeFirebaseUid);  // string (Firebase UID)
        inv.put("inviterUserId",    inviterUserId);
        inv.put("inviteeUserId",    inviteeUserId);
        inv.put("inviterName",      inviterName);
        inv.put("status",           "PENDING");
        inv.put("createdAt",        FieldValue.serverTimestamp());

        fs.collection("allianceInvites").document(docId).set(inv)
                .addOnSuccessListener(unused -> {
                    Log.i("INVITE","sent: "+docId);

                    // upiši LOKALNO u Room (optimistički), u background-u:
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AllianceInvitation ai = new AllianceInvitation();
                        ai.setAllianceId(allianceId);
                        ai.setInviterUserId(inviterUserId);
                        ai.setInviteeUserId(inviteeUserId);
                        ai.setStatus(InvitationStatus.PENDING);

                        db.allianceInvitationRepository().upsert(ai);

                        int c = db.allianceInvitationRepository()
                                .findAllByInviter(inviterUserId).size();
                        Log.w("SVI_REDOVI"," "+c);

                        // osveži UI (označi kliknutog kao PENDING)
                        runOnUiThread(() -> {
                            for (var d : friendDTOs) {
                                if (d.userId == inviteeUserId) {
                                    d.invitationStatus = ftn.project.domain.entity.InvitationStatus.PENDING;
                                    break;
                                }
                            }
                            adapter.replaceAll(friendDTOs);
                        });
                    });
                })
                .addOnFailureListener(e -> Log.e("INVITE","fail: "+e.getMessage(), e));
       }
    private final android.content.BroadcastReceiver allianceStatusReceiver =
            new android.content.BroadcastReceiver() {
                @Override public void onReceive(android.content.Context c, android.content.Intent i) {
                    String inviteId = i.getStringExtra(AllianceInvitationReceiver.EXTRA_INVITE_ID);
                    String status   = i.getStringExtra(AllianceInvitationReceiver.EXTRA_NEW_STATUS);
                    // reci adapteru da promeni tekst dugmeta
                    adapter.updateStatus(inviteId, status);
                }
            };

    @Override protected void onStart() {
        super.onStart();
        IntentFilter f = new IntentFilter(AllianceInvitationReceiver.ACTION_UI_STATUS_CHANGED);
        // samo unutar tvoje app-ke
        ContextCompat.registerReceiver(
                this,
                allianceStatusReceiver,
                f,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    @Override protected void onStop() {
        unregisterReceiver(allianceStatusReceiver);
        super.onStop();
    }
}