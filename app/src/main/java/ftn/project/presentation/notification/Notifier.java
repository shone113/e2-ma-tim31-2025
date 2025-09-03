package ftn.project.presentation.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ftn.project.MyApp;
import ftn.project.R;
import ftn.project.presentation.ui.ChatActivity;

public final class Notifier {
    private Notifier() {}

    public static void showInvite(Context ctx,
                                  String inviteId,
                                  int allianceId,
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

    public static void showChatMessage(Context ctx,
                                       int messageId,
                                       int allianceId,
                                       String content,
                                       int creatorUserId,
                                       String creatorUsername,
                                       Long sentAt,
                                       String allianceName
    ) {
        // Otvaranje chat ekrana na klik
        Intent openChat = new Intent(ctx, ftn.project.presentation.ui.ChatActivity.class)
                .putExtra(ChatActivity.EXTRA_ALLIANCE_ID, allianceId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentPi = PendingIntent.getActivity(
                ctx, allianceId, openChat, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Stabilan ID po savezu (da nova poruka zameni prethodnu notifikaciju tog saveza)
        // Ako želiš da svaka poruka bude zasebna notifikacija, koristi messageId.hashCode().
        int nid = ("chat-" + allianceId).hashCode();

        // (Opcionalno) Android MessagingStyle – lepši prikaz konverzacije
        NotificationCompat.MessagingStyle style =
                new NotificationCompat.MessagingStyle(creatorUsername) // "ja" ako želiš Person za sebe
                        .setConversationTitle(allianceName)
                        .setGroupConversation(true)
                        .addMessage(content, sentAt, creatorUsername);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, MyApp.CHANNEL_MESSAGES)
                .setSmallIcon(R.drawable.chat) // zameni svojom ikonom
                .setContentTitle(allianceName)
                .setContentText(creatorUsername + ": " + content)
                .setStyle(style) // ili .setStyle(new BigTextStyle().bigText(senderName + ": " + previewText))
                .setWhen(sentAt)
                .setShowWhen(true)
                .setContentIntent(contentPi)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT); // stavi HIGH ako želiš heads-up

        NotificationManagerCompat.from(ctx).notify(nid, b.build());
    }
    // Notifier.java
    public static void showInvitationRespond(
            Context ctx,
            int allianceId,
            @Nullable String allianceName,
            int invitationId,
            String inviteeName,
            String status
    ) {
        String text = "";
        if(status.equals("Accepted")){
            text  = inviteeName + " joined in " + (allianceName != null ? " " + allianceName : "");
        }else{
            text  = inviteeName + " canceled invitation in " + (allianceName != null ? " " + allianceName : "");
        }

        String channelId = MyApp.CHANNEL_RESPOND;
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.drawable.group)
                .setContentTitle(status)
                .setContentText(text)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_EVENT);

        int notifId = 30_000 + invitationId;
        NotificationManagerCompat.from(ctx).notify(notifId, b.build());
    }

}
