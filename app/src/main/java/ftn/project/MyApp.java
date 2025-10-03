package ftn.project;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.concurrent.Executors;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Alliance;
import ftn.project.domain.entity.User;

public class MyApp extends Application {
    public static final String CHANNEL_INVITES = "invites";
    public static final String CHANNEL_MESSAGES = "messages";
    public static final String CHANNEL_RESPOND = "respond";

    private AppDatabase db;
    private ListenerRegistration chatReg;
    private long sessionStartMs;
    private ListenerRegistration inviteRespondReg;
    private FirebaseUser fu;
    private  com.google.firebase.Timestamp startTime;

    @Override public void onCreate() {
        super.onCreate();
        createInviteChannel();
        createMessageChannel();
        createInvitationRespondChannel();
        db = AppDatabase.getInstance(getApplicationContext());
        sessionStartMs = System.currentTimeMillis();

        startTime = com.google.firebase.Timestamp.now();

        FirebaseAuth.getInstance().addAuthStateListener(auth -> {
            fu = auth.getCurrentUser();
            if (fu == null) {
                Log.i("MyApp", "Logout detektovan -> gasim chat listener");
                tearDownChatListener();
                return;
            }
            Log.i("MyApp", "User ulogovan: " + fu.getUid());

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(fu.getUid())               // ako ti je docId = uid
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) return;
                        Long intIdL = doc.getLong("intId");
                        if (intIdL == null) return;
                        int intId = Math.toIntExact(intIdL);

                        resolveAllianceAndStartListener(fu.getUid(), intId);
                        startInviteRespondListener(fu.getUid());
                    });
        });
    }

    private void createInviteChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_INVITES, "Alliance invites", NotificationManager.IMPORTANCE_HIGH
            );
            ch.setDescription("Obaveštenja o pozivima u savez");
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    private void createMessageChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_MESSAGES, "Alliance messages", NotificationManager.IMPORTANCE_HIGH
            );
            ch.setDescription("Poruke u savezu");
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    private void createInvitationRespondChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_RESPOND, "Invitation respond", NotificationManager.IMPORTANCE_HIGH
            );
            ch.setDescription("Invitation respond");
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    /** Ugasiti prethodni listener ako postoji (na logout/promeni naloga). */
    private void tearDownChatListener() {
        if (chatReg != null) {
            chatReg.remove();
            chatReg = null;
        }
    }

    /** 1) U pozadini dohvatamo lokalnog user-a i savez; 2) na glavnoj niti pokrećemo listener tek kad imamo ID. */
    private void resolveAllianceAndStartListener(String firebaseUid, int userId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            @Nullable User me = db.userRepository().getByFirebaseUid(firebaseUid);
            if (me == null) {
                Log.w("MyApp", "Lokalni User ne postoji još za uid=" + firebaseUid + " -> ne pokrećem chat listener");
                postMain(() -> tearDownChatListener());
                return;
            }

            Integer aid = me.getAllianceId();
            if (aid == null) {
                Log.i("MyApp", "Korisnik nema allianceId -> preskačem chat listener");
                postMain(() -> tearDownChatListener());
                return;
            }

            @Nullable Alliance alliance = db.allianceRepository().getAlliance(aid);
            if (alliance == null) {
                Log.w("MyApp", "Alliance ne postoji lokalno za id=" + aid + " -> preskačem listener");
                postMain(() -> tearDownChatListener());
                return;
            }

            postMain(() -> startChatListener(alliance, userId));
        });
    }

    /** Pokreće Firestore listener za konkretan savez. */
    private void startChatListener(Alliance alliance, int userId) {
        tearDownChatListener(); // sigurnost da nema duplih

        int allianceId = alliance.getAllianceId();
        String allianceName = alliance.getName();

        FirebaseFirestore dbf = FirebaseFirestore.getInstance();
        chatReg = dbf.collection("messages")
                .whereEqualTo("allianceId", allianceId)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;

                    for (DocumentChange dc : snap.getDocumentChanges()) {
                        if (dc.getType() != DocumentChange.Type.ADDED) continue;

                        DocumentSnapshot d = dc.getDocument();

                        Long creatorIdLong  = d.getLong("creatorUserId");
                        int creatorUserId   = creatorIdLong != null ? creatorIdLong.intValue() : -1;

                        boolean isMine = (userId != -1 && creatorUserId == userId);

                        Long allianceIdL = d.getLong("allianceId");
                        if (allianceIdL == null) continue;

                        int gotAllianceId = Math.toIntExact(allianceIdL);
                        Long messageIdLong = d.getLong("messageId");
                        int messageId = messageIdLong != null ? messageIdLong.intValue() : 0;

                        String content         = d.getString("content");
                        String creatorUsername = d.getString("creatorUsername");
                        Long sentAt            = d.getLong("sentAt");

                        Executors.newSingleThreadExecutor().execute(() -> {
                            ftn.project.domain.entity.AllianceMessage m = new ftn.project.domain.entity.AllianceMessage();
                            m.setMessageId(messageId);
                            m.setAllianceId(gotAllianceId);
                            m.setCreatorUserId(creatorUserId);
                            m.setCreatorUsername(creatorUsername);
                            m.setContent(content);
                            m.setSentAt(sentAt);

                            db.allianceMessageRepository().insert(m);
                        });
                        if(!isMine &&  sentAt >= sessionStartMs){
                            ftn.project.presentation.notification.Notifier.showChatMessage(
                                    getApplicationContext(),
                                    messageId,
                                    gotAllianceId,
                                    content,
                                    creatorUserId,
                                    creatorUsername,
                                    sentAt,
                                    allianceName
                            );
                        }
                    }
                });

        Log.i("MyApp", "Chat listener startovan za allianceId=" + allianceId);
    }
    private void tearDownInviteRespondListener() {
        if (inviteRespondReg != null) { inviteRespondReg.remove(); inviteRespondReg = null; }
    }
    private void startInviteRespondListener(String myUid) {
        tearDownInviteRespondListener();

        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        // warm-up: prvi snapshot (postojeći dokumenti) preskačemo
        final boolean[] warmed = { false };

        inviteRespondReg = fs.collection("allianceInvites")
                .whereEqualTo("inviterUid", myUid)
//                .whereEqualTo("status", "ACCEPTED")
//                .whereGreaterThan("respondedAt", startTime)
//                .orderBy("respondedAt")
//                .startAfter(startTime)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    //if (!warmed[0]) { warmed[0] = true; return; } // ne diži retro obaveštenja

                    for (com.google.firebase.firestore.DocumentChange dc : snap.getDocumentChanges()) {

                        if (dc.getType() != com.google.firebase.firestore.DocumentChange.Type.MODIFIED) continue;

                        var d = dc.getDocument();
                        String invStatus = d.getString("status");

                        // Ovde reaguješ SAMO kad status postane ACCEPTED
                        if ("ACCEPTED".equals(invStatus)) {

                            // Polja iz dokumenta (pretpostavljamo da ih već upisuješ)
                            Long aIdL = d.getLong("allianceId");
                            if (aIdL == null) continue;
                            int allianceId = aIdL.intValue();

                            Long invIdL = d.getLong("invitationId");
                            int invitationId = invIdL != null ? invIdL.intValue() : 0;

                            String allianceName = d.getString("allianceName");
                            if (allianceName == null) allianceName = "Savez";

                            String inviteeName = d.getString("inviteeName");
                            if (inviteeName == null) inviteeName = "Korisnik";

                            Long inviterUserIdLong = d.getLong("inviterUserId");
                            int inviterUserId = inviterUserIdLong != null ? inviterUserIdLong.intValue() : 0;

                            String status = d.getString("status"); // "ACCEPTED" ili "NONE"

                            String inviteStatus = "ACCEPTED".equals(status) ? "Accepted" : "Declined";

                            // Notifikacija kreatoru saveza
                            User user = db.userRepository().getByFirebaseUid(fu.getUid());
//                        if(inviterUserId == user.getUserId())
                            ftn.project.presentation.notification.Notifier.showInvitationRespond(
                                    getApplicationContext(),
                                    allianceId,
                                    allianceName,
                                    invitationId,
                                    inviteeName,
                                    inviteStatus
                            );
                        }
                    }
                });
    }

    private void postMain(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }
}
