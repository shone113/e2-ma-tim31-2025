package ftn.project.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ftn.project.R; // obavezno da importuješ R iz svog paketa
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Alliance;
import ftn.project.domain.entity.SpecialMission;
import ftn.project.domain.entity.SpecialMissionProgress;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.LoggedUserService;
import ftn.project.domain.worker.MissionEndWorker;

public class MainActivity extends AppCompatActivity {

    private Button btnActSpecMission;

    private void scheduleMissionEndWorker(int missionId, LocalDateTime endDate) {
        long delayMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), endDate);

        if (delayMillis <= 0) {
            // Ako je iz nekog razloga endDate već prošao
            return;
        }

        Data inputData = new Data.Builder()
                .putInt("missionId", missionId)
                .build();

        OneTimeWorkRequest workRequest =
                new OneTimeWorkRequest.Builder(MissionEndWorker.class)
                        .setInputData(inputData)
                        .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(this).enqueue(workRequest);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppDatabase db = AppDatabase.getInstance(this);
        setContentView(R.layout.activity_main);
        btnActSpecMission = findViewById(R.id.btnActSpecMission);
        LoggedUserService loggedUserService = new LoggedUserService(this);
        User logged = loggedUserService.getCurrentUser();
        Alliance alliance = db.allianceRepository().getAlliance(logged.getUserId());

        List<SpecialMission> activeSpecialMission = db.specialMissionRepository().activeSpecialMissionByAlliance(alliance.getAllianceId());
        if(logged.getUserId() == alliance.getLeaderUserId() || activeSpecialMission.isEmpty())
            btnActSpecMission.setVisibility(View.VISIBLE);
        else
            btnActSpecMission.setVisibility(View.GONE);

        int allianceCount = db.userRepository().allianceCount(alliance.getAllianceId());

        btnActSpecMission.setOnClickListener(v -> {
            List<User> allianceUsers = db.userRepository().getAllUserInAlliance(alliance.getAllianceId());
            SpecialMission specialMission = new SpecialMission(
                    0,
                    alliance.getAllianceId(),
                    allianceCount * 100,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusWeeks(2),
                    true
            );
            long missionId = db.specialMissionRepository().insert(specialMission);
            for(User u : allianceUsers)
            {
                SpecialMissionProgress specialMissionProgress = new SpecialMissionProgress(
                        0,
                        (int)missionId,
                        u.getUserId(),
                        0,
                        0,
                        0,
                        0,
                        0,
                        true,
                        0
                );
                db.specialMissionProgressRepository().insert(specialMissionProgress);
            }
            scheduleMissionEndWorker((int)missionId, specialMission.getEndDate());
        });
    }
}
