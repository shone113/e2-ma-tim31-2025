package ftn.project.presentation.ui;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

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
import ftn.project.domain.entity.AllianceMessage;
import ftn.project.domain.entity.Friendship;
import ftn.project.domain.entity.InvitationStatus;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.AllianceService;
import ftn.project.domain.usecase.FriendshipService;
import ftn.project.presentation.adapter.FriendAdapter;
import ftn.project.presentation.notification.AllianceInvitationReceiver;

public class AllianceActivity extends AppCompatActivity {
    public static final String EXTRA_ALLIANCE_ID = "extra_alliance_id";

    private FriendAdapter adapter;
    private ArrayList<UserFriendDTO> friendDTOs;
    private FriendshipService friendshipService;
    private AllianceService allianceService;
    private AppDatabase db;
    private User loggedUser;
    private ListenerRegistration sentInvitesReg;
    private Integer currentAllianceId;


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
        allianceService = new AllianceService(this);

        int fromIntent = getIntent().getIntExtra(EXTRA_ALLIANCE_ID, -1);
        if (fromIntent != -1) {
            currentAllianceId = fromIntent;
        } else if (loggedUser != null) {
            currentAllianceId = loggedUser.getAllianceId();
        }

        MaterialButton btnCreate = findViewById(R.id.btnCreateAlliance);
        MaterialButton btnDisband = findViewById(R.id.btnDisbandAlliance);
        TextInputEditText etName = findViewById(R.id.etAllianceName);
        MaterialButton btnChat = findViewById(R.id.btnChat);
        if(currentAllianceId == null){
            btnChat.setEnabled(false);
        }else{
            btnChat.setEnabled(true);
        }

        if(loggedUser.getAllianceId() != null){
            Alliance alliance = db.allianceRepository().getAlliance(loggedUser.getAllianceId());
            etName.setText(alliance.getName());
            etName.setEnabled(false);
            btnCreate.setVisibility(View.GONE);
            showFriends(alliance.getAllianceId(), alliance.getName());
            if(alliance.getLeaderUserId() == loggedUser.getUserId()){
                btnDisband.setVisibility(View.VISIBLE);
            }else{
                btnDisband.setVisibility(View.GONE);
            }
        }

        btnCreate.setOnClickListener(v -> {
            etName.setEnabled(false);
            btnCreate.setVisibility(View.GONE);
            btnDisband.setVisibility(View.VISIBLE);

            Alliance alliance = new Alliance();

            alliance.setName(etName.getText().toString());
            alliance.setLeaderUserId(loggedUser.getUserId());

            allianceService.createAlliance(etName.getText().toString(),
                            loggedUser.getUserId(),
                            () -> Toast.makeText(getApplicationContext(), "Alliances synced ✔", Toast.LENGTH_SHORT).show()
                    )
                    .addOnSuccessListener(allianceId -> {
                        alliance.setAllianceId(allianceId);
                        currentAllianceId = (int)db.allianceRepository().insert(alliance);
                        db.userRepository().updateAllianceId(loggedUser.getUserId(), currentAllianceId);

                        updateCreateButtonUI(currentAllianceId, btnCreate, btnDisband, etName);
                        showFriends(currentAllianceId, alliance.getName());

                        btnChat.setEnabled(true);
                    });

        });

        //DISBAND ALLIANCEEE
        btnDisband.setOnClickListener(v -> {
            etName.setEnabled(false);
            btnDisband.setVisibility(View.VISIBLE);
            btnDisband.setEnabled(false);
            db.allianceRepository().disbandAlliance(currentAllianceId);
        });

        btnChat.setOnClickListener(v -> {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra(ChatActivity.EXTRA_ALLIANCE_ID, currentAllianceId);
            this.startActivity(i);
        });
    }

    public void updateCreateButtonUI(
            int newAllianceId,
            MaterialButton btnCreate,
            MaterialButton btnDisband,
            TextInputEditText etName){

        if(newAllianceId <= 0){
            btnCreate.setVisibility(View.VISIBLE);
            btnDisband.setVisibility(View.GONE);
            etName.setEnabled(true);
            Log.e("AllianceActivity","Alliance insert failed");
        }
    }
    public void showFriends(int allianceId, String allianceName){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        ArrayList<Friendship> friendships = new ArrayList<>(db.friendshipRepository().getAllForUserId(loggedUser.getUserId()));
        List<User> users = db.userRepository().getAll();
        List<AllianceInvitation> allianceInvitations = db.allianceInvitationRepository().findAllByInviter(loggedUser.getUserId());
        Alliance alliance = db.allianceRepository().getAlliance(allianceId);
        boolean leaderUser = alliance.getLeaderUserId() == loggedUser.getUserId() ? true : false;
        friendDTOs = friendshipService.getFriendsWithInvitationForUser(friendships, users, loggedUser.getUserId(), allianceInvitations);

        adapter = new FriendAdapter(this, friendDTOs, leaderUser, alliance.getLeaderUserId(), userFriendDTO -> {
            Log.w("ISPIS", "IZVRSIO SAM SE");
            Friendship friendship = new Friendship();
            friendship.setFirstUserId(loggedUser.getUserId());
            friendship.setSecondUserId(userFriendDTO.userId);
            long rowId = db.friendshipRepository().insert(friendship);
            User friend = db.userRepository().getById(friendship.secondUserId);
            sendInvite(
                    friend.getFirebaseUid(),
                    userFriendDTO.userId,
                    allianceId,
                    allianceName,
                    loggedUser.getUsername(),
                    loggedUser.getUserId(),
                    friend.getUsername()
            );
        });

        ListView lvFriends = findViewById(R.id.lvFriends);
        if (lvFriends != null) {
            lvFriends.setAdapter(adapter);
            attachSentInvitesListener();
        }
    }
    private void sendInvite(
            String inviteeFirebaseUid,
            int inviteeUserId,
            int allianceId,
            String allianceName,
            String inviterName,
            int inviterUserId,
            String inviteeName
    ) {
        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        String docId = fs.collection("allianceInvites").document().getId();
        FirebaseFirestore fdb = FirebaseFirestore.getInstance();

        String inviterUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Executors.newSingleThreadExecutor().execute(() -> {
            AllianceInvitation ai = new AllianceInvitation();
            ai.setAllianceId(allianceId);
            ai.setInviterUserId(inviterUserId);
            ai.setInviteeUserId(inviteeUserId);
            ai.setStatus(InvitationStatus.PENDING);

            long invitationId = db.allianceInvitationRepository().upsert(ai);

            Map<String, Object> inv = new HashMap<>();
            inv.put("invitationId", invitationId);
            inv.put("allianceId", allianceId);
            inv.put("allianceName", allianceName);
            inv.put("inviterUid", inviterUid);          // string (Firebase UID)
            inv.put("inviteeUid", inviteeFirebaseUid);  // string (Firebase UID)
            inv.put("inviterUserId", inviterUserId);
            inv.put("inviteeUserId", inviteeUserId);
            inv.put("inviterName", inviterName);
            inv.put("inviteeName", inviteeName);
            inv.put("status", "PENDING");
            inv.put("createdAt", FieldValue.serverTimestamp());

            fs.collection("allianceInvites").document(docId).set(inv)
                    .addOnSuccessListener(unused -> {
                        Log.i("INVITE", "sent: " + docId);


                    })
                    .addOnFailureListener(e -> Log.e("INVITE", "fail: " + e));

        });
    }

    private void attachSentInvitesListener() {
        if (sentInvitesReg != null) return;           // već aktivan
        if (adapter == null) return;                  // još nemamo adapter -> nema listeniranja

        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        sentInvitesReg = fs.collection("allianceInvites")
                .whereEqualTo("inviterUid", myUid)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    if (adapter == null || friendDTOs == null) return;

                    for (DocumentChange dc : snap.getDocumentChanges()) {
                        String statusStr = dc.getDocument().getString("status"); // "PENDING" | "ACCEPTED" | "DECLINED"
                        Number inviteeUserIdNum = (Number) dc.getDocument().get("inviteeUserId");
                        if (inviteeUserIdNum == null || statusStr == null) continue;

                        int inviteeUserId = inviteeUserIdNum.intValue();
                        updateInvitationStatus(inviteeUserId, statusStr);
                    }
                });
    }

    private void detachSentInvitesListener() {
        if (sentInvitesReg != null) { sentInvitesReg.remove(); sentInvitesReg = null; }
    }

    private void updateInvitationStatus(int inviteeUserId, String statusStr) {
        // Ako koristiš enum InvitationStatus, prebaci:
        InvitationStatus st;
        try { st = InvitationStatus.valueOf(statusStr); } catch (Exception ex) { st = InvitationStatus.PENDING; }

        for (UserFriendDTO d : friendDTOs) {
            if (d.userId == inviteeUserId) {
                d.invitationStatus = st;
                break;
            }
        }
        runOnUiThread(() -> { if (adapter != null) adapter.replaceAll(friendDTOs); });
    }
}