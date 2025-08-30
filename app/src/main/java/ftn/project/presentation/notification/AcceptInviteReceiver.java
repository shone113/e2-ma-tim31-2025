package ftn.project.presentation.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import ftn.project.domain.worker.AcceptInviteWorker;

public class AcceptInviteReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context ctx, Intent i) {
        String inviteId = i.getStringExtra("inviteId");
        int nid = i.getIntExtra("notificationId", 0);

        OneTimeWorkRequest w = new OneTimeWorkRequest.Builder(AcceptInviteWorker.class)
                .setInputData(new Data.Builder()
                        .putString("inviteId", inviteId)
                        .putInt("notificationId", nid)
                        .build())
                .build();
        WorkManager.getInstance(ctx).enqueue(w);
    }
}
