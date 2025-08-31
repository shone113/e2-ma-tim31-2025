package ftn.project.domain.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import ftn.project.data.db.AppDatabase;
import ftn.project.data.repository.AllianceInvitationRepository;
import ftn.project.domain.usecase.AcceptAllianceInviteService;

public class AcceptInviteWorker extends Worker {

    private AcceptAllianceInviteService acceptAllianceInviteService;
    public AcceptInviteWorker(@NonNull Context ctx, @NonNull WorkerParameters p) {
        super(ctx, p);
        AppDatabase db = AppDatabase.getInstance(ctx.getApplicationContext());
        acceptAllianceInviteService = new AcceptAllianceInviteService(db.allianceInvitationRepository());
    }

    @NonNull
    @Override public Result doWork() {
        Data in = getInputData();
        int inviteId = in.getInt("inviteId", -1);
        int nid = in.getInt("notificationId", 0);

        try {
            acceptAllianceInviteService.execute(inviteId);
            NotificationManagerCompat.from(getApplicationContext()).cancel(nid);
            return Result.success();
        } catch (Exception e) {
            // Po potrebi: retry ili failure
            return Result.retry();
        }
    }
}
