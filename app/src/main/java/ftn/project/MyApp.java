package ftn.project;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MyApp extends Application {
    public static final String CHANNEL_INVITES = "invites";

    @Override public void onCreate() {
        super.onCreate();
        createInviteChannel();
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
}
