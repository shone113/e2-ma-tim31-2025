package ftn.project.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ftn.project.R;
import ftn.project.data.database.FirestoreSync;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Alliance;
import ftn.project.domain.entity.SpecialMission;
import ftn.project.domain.entity.SpecialMissionProgress;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.BattleStartService;
import ftn.project.domain.usecase.LoggedUserService;
import ftn.project.domain.worker.MissionEndWorker;
import ftn.project.presentation.notification.Notifier;

public class MainActivity extends AppCompatActivity {

    private LinearLayout btnActSpecMission;
    private LinearLayout btnTaskCalendar, btnShop, btnNewTask, btnBattle, btnCategories, btnAllUsers;
    private ListenerRegistration inviteReg;
    private AppDatabase db;

    private void scheduleMissionEndWorker(int missionId, LocalDateTime endDate) {
        long delayMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), endDate);

        if (delayMillis <= 0) {
            return;
        }

        Data inputData = new Data.Builder()
                .putInt("missionId", missionId)
                .build();

        OneTimeWorkRequest workRequest =
                new OneTimeWorkRequest.Builder(MissionEndWorker.class)
                        .setInputData(inputData)
                        .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(this).enqueue(workRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getInstance(this);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != getPackageManager().PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        FirestoreSync.syncAllAlliancesDown(
                getApplicationContext(),
                db,
                () -> Toast.makeText(this, "Friendships synced ✔", Toast.LENGTH_SHORT).show()
        );

        // --- Dugmad ---
        btnActSpecMission = findViewById(R.id.btnActSpecMission);
        btnTaskCalendar = findViewById(R.id.btnTaskCalendar);
        btnShop = findViewById(R.id.btnShop);
        btnNewTask = findViewById(R.id.btnNewTask);
        btnBattle = findViewById(R.id.btnBattle);
        btnCategories = findViewById(R.id.btnCategories);
        btnAllUsers = findViewById(R.id.btnAllUsers);

        // --- Provera lidera i specijalne misije ---
        LoggedUserService loggedUserService = new LoggedUserService(this);
        User logged = loggedUserService.getCurrentUser();
        Alliance alliance = db.allianceRepository().getAllianceByUser(logged.getUserId());

        if(alliance == null){
            btnActSpecMission.setVisibility(View.GONE);
        }else{
            List<SpecialMission> activeSpecialMission =
                    db.specialMissionRepository().activeSpecialMissionByAlliance(alliance.getAllianceId());

            if (logged.getUserId() == alliance.getLeaderUserId() && activeSpecialMission.isEmpty())
                btnActSpecMission.setVisibility(View.VISIBLE);
            else
                btnActSpecMission.setVisibility(View.GONE);

            int allianceCount= db.userRepository().allianceCount(alliance.getAllianceId());


            // --- Specijalna misija ---
            btnActSpecMission.setOnClickListener(v -> {
                List<User> allianceUsers = db.userRepository().getAllUserInAlliance(alliance.getAllianceId());
                SpecialMission specialMission = new SpecialMission(
                        0,
                        alliance.getAllianceId(),
                        allianceCount * 100,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusWeeks(2),
                        true
                );
                long missionId = db.specialMissionRepository().insert(specialMission);

                for (User u : allianceUsers) {
                    SpecialMissionProgress specialMissionProgress = new SpecialMissionProgress(
                            0,
                            (int) missionId,
                            u.getUserId(),
                            0, 0, 0, 0, 0,
                            true,
                            0
                    );
                    db.specialMissionProgressRepository().insert(specialMissionProgress);
                }

                scheduleMissionEndWorker((int) missionId, specialMission.getEndDate());
            });
        }

        // --- Ostala dugmad (otvaraju activity-je) ---
        btnTaskCalendar.setOnClickListener(v -> {
            startActivity(new Intent(this, TaskCalendarActivity.class));
        });

        btnShop.setOnClickListener(v -> {
            startActivity(new Intent(this, ShopActivity.class));
        });

        btnNewTask.setOnClickListener(v -> {
            startActivity(new Intent(this, NewTaskActivity.class));
        });

        btnBattle.setOnClickListener(v -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) return; // safety check

            String firebaseUid = firebaseUser.getUid();
            User currentUser = db.userRepository().getByFirebaseUid(firebaseUid);

            BattleStartService starter = new BattleStartService(this);
            BattleStartService.BattleStartResult result = starter.startNewBattle(currentUser);

            Intent intent = new Intent(this, BattleActivity.class);
            intent.putExtra("battleId", result.battleId);
            intent.putExtra("hitChance", result.hitChance);
            startActivity(intent);
        });

        btnCategories.setOnClickListener(v -> {
            startActivity(new Intent(this, CategoryListActivity.class));
        });

        btnAllUsers.setOnClickListener(v -> {
            startActivity(new Intent(this, AllUsersActivity.class));
        });
    }

    @Override protected void onStart() {
        super.onStart();

        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore dbf = FirebaseFirestore.getInstance();

        inviteReg = dbf.collection("allianceInvites")
                .whereEqualTo("inviteeUid", myUid)
                .whereEqualTo("status", "PENDING")
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;

                    for (DocumentChange dc : snap.getDocumentChanges()) {
                        if (dc.getType() != DocumentChange.Type.ADDED) continue;

                        DocumentSnapshot d = dc.getDocument();
                        Long allianceIdL = d.getLong("allianceId");           // Firestore number -> Long
                        if (allianceIdL == null) {
                            Log.w("INVITES", "invite bez allianceId: " + d.getId());
                            return; // ili continue; ako si u for-petlji
                        }

                        Long invitationIdLong = d.getLong("invitationId");
                        int invitationId = 0;
                        if (invitationIdLong != null) {
                            invitationId = invitationIdLong.intValue(); // konvertuje Long u int
                        }

                        int allianceId = Math.toIntExact(allianceIdL);
                        String inviteId     = d.getId();
                        String allianceName = d.getString("allianceName");
                        String inviterUid   = d.getString("inviterUid");
                        String inviterName  = d.getString("inviterName");
                        Long inviteeUserId = d.getLong("inviteeUserId");

                        // lokalna notifikacija na PRIMAOČU
                        ftn.project.presentation.notification.Notifier.showInvite(
                                getApplicationContext(),
                                inviteId,
                                String.valueOf(allianceId),
                                allianceName,
                                inviterUid,
                                inviterName,
                                invitationId,
                                inviteeUserId.intValue()
                        );
                    }
                });
    }

    @Override protected void onStop() {
        if (inviteReg != null) { inviteReg.remove(); inviteReg = null; }
        super.onStop();
    }
}
