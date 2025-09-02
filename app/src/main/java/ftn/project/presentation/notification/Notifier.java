package ftn.project.presentation.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ftn.project.MyApp;
import ftn.project.R;

public final class Notifier {
    private Notifier() {}

    public static void showInvite(Context ctx,
                                  String inviteId,
                                  String allianceId,
                                  String allianceName,
                                  String inviterUid,
                                  String inviterName,
                                  int invitationId,
                                  int inviteeUserId) {

        int nid = (inviteId != null ? inviteId.hashCode() : (int)System.currentTimeMillis());

        // Accept
        Intent accept = new Intent(ctx, AllianceInvitationReceiver.class)
                .setAction("ftn.project.ACTION_ACCEPT_INVITE")
                .putExtra("inviteId", inviteId)
                .putExtra("allianceId", allianceId)
                .putExtra("invitationId", invitationId)
                .putExtra("inviteeUserId", inviteeUserId)
                .putExtra("notificationId", nid);
        PendingIntent piAccept = PendingIntent.getBroadcast(
                ctx, nid, accept, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Decline
        Intent decline = new Intent(ctx, AllianceInvitationReceiver.class)
                .setAction("ftn.project.ACTION_DECLINE_INVITE")
                .putExtra("inviteId", inviteId)
                .putExtra("notificationId", nid);
        PendingIntent piDecline = PendingIntent.getBroadcast(
                ctx, nid + 1, decline, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = ctx.getString(R.string.notif_title_invite);
        String body = ctx.getString(R.string.notif_body_invite,
                allianceName != null ? allianceName : "savez",
                inviterName != null ? inviterName : "nepoznat");

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, MyApp.CHANNEL_INVITES)
                .setSmallIcon(R.drawable.ic_invite)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setOnlyAlertOnce(true)
                .setOngoing(true)            // ➜ nedismissable dok ne klikneš
                .setAutoCancel(false)
                .addAction(R.drawable.ic_check, ctx.getString(R.string.notif_action_accept), piAccept)
                .addAction(R.drawable.ic_close, ctx.getString(R.string.notif_action_decline), piDecline);

        NotificationManagerCompat.from(ctx).notify(nid, b.build());
    }
}
