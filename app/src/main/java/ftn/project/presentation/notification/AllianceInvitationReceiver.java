package ftn.project.presentation.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class AllianceInvitationReceiver extends BroadcastReceiver {
    public static final String ACTION_UI_STATUS_CHANGED = "ftn.project.ACTION_UI_STATUS_CHANGED";
    public static final String EXTRA_NEW_STATUS = "extra_new_status";
    public static final String EXTRA_INVITE_ID = "extra_invite_id";

    @Override public void onReceive(Context ctx, Intent intent) {
        String action   = intent.getAction();
        String inviteId = intent.getStringExtra("inviteId");
        int nid         = intent.getIntExtra("notificationId", 0);
        if (inviteId == null) return;

        String newStatus = "NONE";
        if ("ftn.project.ACTION_ACCEPT_INVITE".equals(action)) newStatus = "ACCEPTED";

        FirebaseFirestore.getInstance()
                .collection("allianceInvites")
                .document(inviteId)
                .update("status", newStatus);

        if (nid != 0) NotificationManagerCompat.from(ctx).cancel(nid);
    }
}
