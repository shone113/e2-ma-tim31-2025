package ftn.project;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Alliance;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.AllianceMessageService;

public class MyApp extends Application {
    public static final String CHANNEL_INVITES = "invites";
    public static final String CHANNEL_MESSAGES = "messages";

    @Override public void onCreate() {
        super.onCreate();
        createInviteChannel();
        createMessageChannel();
        setupChatListener();
    }

    private void createInviteChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_INVITES, "Alliance invites", NotificationManager.IMPORTANCE_HIGH
            );
            ch.setDescription("Obaveštenja o pozivima u savez");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(ch);
        }
    }
    public void createMessageChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_MESSAGES, "Alliance messages", NotificationManager.IMPORTANCE_HIGH
            );
            ch.setDescription("Poruke u savezu");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(ch);
        }
    }

    private void setupChatListener() {
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore dbf = FirebaseFirestore.getInstance();

        Alliance alliance = getMyAllianceId();
        dbf.collection("messages")
                .whereEqualTo("allianceId", alliance.getAllianceId())
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;

                    for (DocumentChange dc : snap.getDocumentChanges()) {
                        if (dc.getType() != DocumentChange.Type.ADDED) continue;

                        DocumentSnapshot d = dc.getDocument();
                        Long allianceIdL = d.getLong("allianceId");
                        if (allianceIdL == null) continue;

                        int allianceId = Math.toIntExact(allianceIdL);
                        Long messageIdLong = d.getLong("messageId");
                        int messageId = messageIdLong != null ? messageIdLong.intValue() : 0;

                        Long creatorIdLong = d.getLong("creatorUserId");
                        int creatorUserId = creatorIdLong != null ? creatorIdLong.intValue() : 0;

                        String content         = d.getString("content");
                        String creatorUsername = d.getString("creatorUsername");
                        Long sentAt            = d.getLong("sentAt");

                        ftn.project.presentation.notification.Notifier.showChatMessage(
                                getApplicationContext(),
                                messageId,
                                allianceId,
                                content,
                                creatorUserId,
                                creatorUsername,
                                sentAt,
                                alliance.getName()
                        );
                    }
                });
    }

    private Alliance getMyAllianceId() {
        AppDatabase db = AppDatabase.getInstance(this);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        User u = db.userRepository().getByFirebaseUid(firebaseUser.getUid());
        return db.allianceRepository().getAlliance(u.getAllianceId());
    }
}
