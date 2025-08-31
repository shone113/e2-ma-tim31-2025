package ftn.project.domain.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.usecase.AcceptAllianceInviteService;
import ftn.project.domain.usecase.DeclineAllianceInviteService;

public class DeclineInviteWorker extends Worker {
    private DeclineAllianceInviteService declineAllianceInviteService;
    public DeclineInviteWorker(@NonNull Context ctx, @NonNull WorkerParameters p) {
        super(ctx, p);
        AppDatabase db = AppDatabase.getInstance(ctx.getApplicationContext());
        declineAllianceInviteService = new DeclineAllianceInviteService(db.allianceInvitationRepository());
    }

    @NonNull @Override public Result doWork() {
        Data in = getInputData();
        int inviteId = in.getInt("inviteId", -1);
        int nid = in.getInt("notificationId", 0);

        try {
            declineAllianceInviteService.execute(inviteId);
            NotificationManagerCompat.from(getApplicationContext()).cancel(nid);
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
