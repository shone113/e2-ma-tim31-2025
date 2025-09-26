package ftn.project.presentation.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Alliance;
import ftn.project.domain.entity.SpecialMission;
import ftn.project.domain.entity.SpecialMissionProgress;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.LoggedUserService;
import ftn.project.domain.usecase.SpecialMissionProgressService;

public class SpecialMissionActivity extends AppCompatActivity {

    private ProgressBar bossHpBar;
    private TextView bossHpText;

    private ProgressBar userProgressBar;
    private TextView userTotalDamage;
    private ImageView userAvatar;

    private ProgressBar allianceProgressBar;
    private TextView allianceTotalDamage;
    private ImageView allianceAvatar;

    private ProgressBar memberProgressBar;
    private TextView memberTotalDamage;
    private ImageView memberAvatar;
    private Spinner memberSpinner;
    private SpecialMissionProgressService specialMissionProgressService;
    private LoggedUserService loggedUserService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.special_mission);
        AppDatabase db = AppDatabase.getInstance(this);
        specialMissionProgressService = new SpecialMissionProgressService(db.specialMissionRepository(),
                db.specialMissionProgressRepository(),
                db.taskInstanceRepository(),
                db.allianceMessageRepository());
        loggedUserService = new LoggedUserService(this);
        // Boss HP
        User loggedUser = loggedUserService.getCurrentUser();
        SpecialMission specialMission = specialMissionProgressService.getActiveMission(loggedUser.getUserId());
        Alliance alliance = db.allianceRepository().getAllianceByUser(loggedUser.getUserId());
        int allianceCount = db.userRepository().allianceCount(alliance.getAllianceId());
        SpecialMissionProgress loggedProgress = specialMissionProgressService.getActiveMissionProgress(specialMission.getId(),loggedUser.getUserId());

        bossHpBar = findViewById(R.id.bossHpBar);
        bossHpText = findViewById(R.id.bossHpText);


        int maxBossHp = allianceCount * 100;
        int bossHp = specialMission.getBossHp();
        bossHpBar.setMax(maxBossHp);
        bossHpBar.setProgress(bossHp);
        bossHpText.setText(bossHp + "/" + maxBossHp);

        // User progress
        userProgressBar = findViewById(R.id.userProgressBar);
        userTotalDamage = findViewById(R.id.userTotalDamage);
        userAvatar = findViewById(R.id.userAvatar);

        int maxUserHp = 130;
        userProgressBar.setMax(maxUserHp);
        userProgressBar.setProgress(loggedProgress.getTotalDamage());
        userTotalDamage.setText("Total damage: " + loggedProgress.getTotalDamage());
        String loggedAvatar = loggedUser.getAvatarImage(); // npr. "avatar_5"
        int avatarResId = getResources().getIdentifier(loggedAvatar, "drawable", getPackageName());
        userAvatar.setImageResource(avatarResId);

        // Alliance progress
        allianceProgressBar = findViewById(R.id.allianceProgressBar);
        allianceTotalDamage = findViewById(R.id.allianceTotalDamage);
        allianceAvatar = findViewById(R.id.allianceAvatar);

        int totalProgress = specialMissionProgressService.getTotalProgress(specialMission);
        allianceProgressBar.setMax(allianceCount * 130);
        allianceProgressBar.setProgress(totalProgress);
        allianceTotalDamage.setText("Total damage: " + totalProgress);
        allianceAvatar.setImageResource(R.drawable.avatar_2);

        // Other member progress
        memberProgressBar = findViewById(R.id.memberProgressBar);
        memberTotalDamage = findViewById(R.id.memberTotalDamage);
        memberAvatar = findViewById(R.id.memberAvatar);
        memberSpinner = findViewById(R.id.memberSpinner);

        /*
        memberProgressBar.setMax(100);
        memberProgressBar.setProgress(60);
        memberTotalDamage.setText("Total damage: 890");
        memberAvatar.setImageResource(R.drawable.avatar_1);*/

        List<User> members = db.userRepository().getAllUserInAlliance(alliance.getAllianceId());


        List<String> memberNames = new ArrayList<>();
        for (User member : members) {
            memberNames.add(member.getUsername()); // ili member.getDisplayName()
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                memberNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        memberSpinner.setAdapter(adapter);
        memberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                User selectedMember = members.get(position);

                // Progress
                SpecialMissionProgress memberProgress = specialMissionProgressService
                        .getActiveMissionProgress(specialMission.getId(), selectedMember.getUserId());
                memberProgressBar.setMax(130); // ili maksimalni HP po korisniku
                memberProgressBar.setProgress(memberProgress.getTotalDamage());

                // Total damage
                memberTotalDamage.setText("Total damage: " + memberProgress.getTotalDamage());

                // Avatar
                String avatarName = selectedMember.getAvatarImage();
                int avatarResId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
                memberAvatar.setImageResource(avatarResId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
