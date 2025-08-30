package ftn.project.domain.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import ftn.project.di.Deps;

public class AcceptInviteWorker extends Worker {
    public AcceptInviteWorker(@NonNull Context ctx, @NonNull WorkerParameters p) { super(ctx, p); }

    @NonNull
    @Override public Result doWork() {
        Data in = getInputData();
        String inviteId = in.getString("inviteId");
        int nid = in.getInt("notificationId", 0);

        try {
            Deps.acceptInviteUseCase().execute(inviteId);
            NotificationManagerCompat.from(getApplicationContext()).cancel(nid);
            return Result.success();
        } catch (Exception e) {
            // Po potrebi: retry ili failure
            return Result.retry();
        }
    }
}
