package ftn.project.presentation.notification;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class AppMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage msg) {
        Map<String, String> d = msg.getData();
        if (d == null || d.isEmpty()) return;

        String type = d.get("type");
        if (!"ALLIANCE_INVITE".equals(type)) return;

        String inviteId     = d.get("inviteId");
        String allianceId   = d.get("allianceId");
        String allianceName = d.get("allianceName");
        String inviterUid   = d.get("inviterUid");
        String inviterName  = d.get("inviterName");

        Notifier.showInvite(
                this,
                inviteId,
                allianceId,
                allianceName,
                inviterUid,
                inviterName
        );
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // TODO: pošalji novi FCM token na backend ako je potrebno
    }
}
