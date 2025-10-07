package ftn.project.presentation.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import ftn.project.data.database.FirestoreSync;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Alliance;
import ftn.project.domain.entity.InvitationStatus;
import ftn.project.domain.entity.User;

public class AllianceInvitationReceiver extends BroadcastReceiver {
    public static final String ACTION_UI_STATUS_CHANGED = "ftn.project.ACTION_UI_STATUS_CHANGED";
    public static final String EXTRA_NEW_STATUS = "extra_new_status";
    public static final String EXTRA_INVITE_ID = "extra_invite_id";
    public static final String EXTRA_INVITATION_ID = "extra_invitation_id";
    private AppDatabase db;

    @Override public void onReceive(Context ctx, Intent intent) {
        db = AppDatabase.getInstance(ctx);
        String action   = intent.getAction();
        String inviteId = intent.getStringExtra("inviteId");
        int nid         = intent.getIntExtra("notificationId", 0);
        int invitationId = intent.getIntExtra("invitationId", -1);
        int allianceId = intent.getIntExtra("allianceId", -1);
        int inviteeUserId = intent.getIntExtra("inviteeUserId", -1);

        Log.w("LALALALA", " " + allianceId);

        if (invitationId == -1) return;
        if (inviteId == null) return;

        final PendingResult pr = goAsync();

        if ("ftn.project.ACTION_ACCEPT_INVITE".equals(action)) {
            FirestoreSync.syncAllUsersDown(ctx, db, () ->
                    FirestoreSync.syncAllAlliancesDown(
                        ctx, db,
                        () -> {
                            // (opciono) dodatna garancija da parent postoji
                            // if (db.allianceDao().exists(allianceId) == 0) { /* upsert stub ili retry */ }
                            FirebaseFirestore.getInstance()
                                    .collection("allianceInvites")
                                    .document(inviteId)
                                    .update(
                                            "status", "ACCEPTED",
                                            "respondedAt", com.google.firebase.firestore.FieldValue.serverTimestamp()
                                            )
                                    .addOnSuccessListener(unused ->{
                                        db.allianceInvitationRepository()
                                                .updateStatus(invitationId, InvitationStatus.ACCEPTED);
                                        db.userRepository().updateAllianceId(inviteeUserId, allianceId);

                                    })
                                    .addOnCompleteListener(done -> {
                                        if (nid != 0) NotificationManagerCompat.from(ctx).cancel(nid);
                                        pr.finish();
                                    });
                        })
            );
        } else if ("ftn.project.ACTION_DECLINE_INVITE".equals(action)) {
            FirebaseFirestore.getInstance()
                    .collection("allianceInvites")
                    .document(inviteId)
                    .update("status", "NONE")
                    .addOnSuccessListener(unused -> {
                                db.allianceInvitationRepository()
                                        .updateStatus(invitationId, InvitationStatus.NONE);
                            }
                    )
                    .addOnCompleteListener(done -> {
                        NotificationManagerCompat.from(ctx).cancel(nid);
                        pr.finish();
                    });
        } else {
            pr.finish();
        }
    }
}
