package ftn.project.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.time.LocalDateTime;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.SpecialMission;
import ftn.project.domain.entity.SpecialMissionProgress;
import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.TaskInstance;
import ftn.project.domain.entity.TaskInstanceWithTask;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.BattleStartService;
import ftn.project.domain.usecase.CheckQuotaService;
import ftn.project.domain.usecase.LevelAdvancementService;
import ftn.project.domain.usecase.LoggedUserService;
import ftn.project.domain.usecase.SpecialMissionProgressService;
import ftn.project.presentation.adapter.TaskAdapter;

public class TaskListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<TaskInstanceWithTask> allTasks = new ArrayList<>();
    private List<TaskInstanceWithTask> allActiveTasks = new ArrayList<>();
    private List<TaskInstanceWithTask> filteredTasks = new ArrayList<>();
    private SpecialMissionProgressService smps;
    private LevelAdvancementService levelAdvancementService;
    //private ActivityResultLauncher<Intent> taskDetailsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list);

        recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(filteredTasks, new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(TaskInstanceWithTask taskWithInstance) {
                Intent intent = new Intent(TaskListActivity.this, TaskDetailsActivity.class);
                intent.putExtra("task_instance_id", taskWithInstance.taskInstance.getId());
                startActivity(intent);
            }

            @Override
            public void onTaskDoneClicked(TaskInstanceWithTask taskWithInstance, int position) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    AppDatabase db = AppDatabase.getInstance(TaskListActivity.this);

                    LoggedUserService loggedService = new LoggedUserService(TaskListActivity.this);
                    User user = loggedService.getCurrentUser();
                    smps = new SpecialMissionProgressService(
                            db.specialMissionRepository(),
                            db.specialMissionProgressRepository(),
                            db.taskInstanceRepository(),
                            db.allianceMessageRepository()
                    );
                    levelAdvancementService = new LevelAdvancementService(db.levelRepository());
                    int earnedXp = CheckQuotaService.calculateEarnedXP(taskWithInstance.taskInstance,user.getUserId(),user.getLevel(), db);

                    taskWithInstance.taskInstance.setStatus(TaskInstance.TaskStatusEnum.DONE);
                    taskWithInstance.taskInstance.setEarnedXp(earnedXp);

                    db.taskInstanceRepository().updateStatus(
                            taskWithInstance.taskInstance.getId(),
                            TaskInstance.TaskStatusEnum.DONE
                    );
                    db.taskInstanceRepository().updateEarnedXpAndQuota(
                            taskWithInstance.taskInstance.getId(),
                            earnedXp,
                            taskWithInstance.taskInstance.isWithinQuota()
                    );

                    if (earnedXp > 0) {
                        updateLoggedUserPoints(taskWithInstance.task.getUserId(), earnedXp);
                    }
                    int increment = smps.punchByEasyTaskIncrement(user.getUserId(), taskWithInstance.taskInstance);
                    int incrementHard = smps.punchByHardTaskIncerement(user.getUserId(), taskWithInstance.taskInstance);
                    if (increment > 0) {
                        SpecialMission mission = smps.getActiveMission(user.getUserId());
                        SpecialMissionProgress smp = smps.getActiveMissionProgress(mission.getId(), user.getUserId());

                        smp.setEasyNormalTasks(smp.getEasyNormalTasks() + increment);
                        smp.setTotalDamage(smp.getTotalDamage() + increment);

                        db.specialMissionProgressRepository().update(smp);

                        mission.setBossHp(mission.getBossHp() - increment);
                        db.specialMissionRepository().update(mission);
                    }

                    if(incrementHard > 0)
                    {
                        SpecialMission mission = smps.getActiveMission(user.getUserId());
                        SpecialMissionProgress smp = smps.getActiveMissionProgress(mission.getId(), user.getUserId());

                        smp.setOtherTasks(smp.getOtherTasks() + incrementHard);
                        smp.setTotalDamage(smp.getTotalDamage() + incrementHard * 4);

                        db.specialMissionProgressRepository().update(smp);

                        mission.setBossHp(mission.getBossHp() - incrementHard * 4);
                        db.specialMissionRepository().update(mission);
                    }

                    runOnUiThread(() -> {
                        adapter.notifyItemChanged(position);
                        Toast.makeText(TaskListActivity.this,
                                "Zadatak završen! Dobio si " + earnedXp + " XP",
                                Toast.LENGTH_SHORT).show();
                    });
                });
            }
        });
        recyclerView.setAdapter(adapter);

        Button btnJednokratni = findViewById(R.id.btnJednokratni);
        Button btnPonavljajuci = findViewById(R.id.btnPonavljajuci);
        TextView textType = findViewById(R.id.taskTypeText);

        loadTasksFromDatabase();

        btnJednokratni.setOnClickListener(v -> {
                filterTasks(Task.FrequencyEnum.ONE_TIME);
                textType.setText("Jednokratni zadaci");
            });
        btnPonavljajuci.setOnClickListener(v -> {
                filterTasks(Task.FrequencyEnum.REPEATING);
                textType.setText("Ponavljajuci zadaci");
            });
    }
    /// ///NEKA OVO BUDE OVAKO ZA SAD
    @Override
    protected void onResume() {
        super.onResume();
        TextView textType = findViewById(R.id.taskTypeText);
        textType.setText("Svi zadaci");
        loadTasksFromDatabase();
    }

    private void loadTasksFromDatabase() {
        LocalDateTime now = LocalDateTime.now();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            LoggedUserService loggedService = new LoggedUserService(this);
            User user = loggedService.getCurrentUser();
            allTasks = db.taskInstanceRepository().getAllTaskInstancesWithTask(user.getUserId());
            allActiveTasks.clear();
            for(TaskInstanceWithTask t : allTasks)
            {
                if(t.taskInstance.getEndExecutionTime().plusDays(3).isAfter(now))
                    allActiveTasks.add(t);
            }

            runOnUiThread(() -> {
                filteredTasks.clear();
                filteredTasks.addAll(allActiveTasks);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void updateLoggedUserPoints(int userId, int xPValue) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                runOnUiThread(() -> Toast.makeText(this, "Nema aktivnog korisnika!", Toast.LENGTH_SHORT).show());
                return;
            }

            String firebaseUid = firebaseUser.getUid();
            User currentUser = db.userRepository().getByFirebaseUid(firebaseUid);

            if (currentUser == null) {
                runOnUiThread(() -> Toast.makeText(this, "Korisnik nije pronađen!", Toast.LENGTH_SHORT).show());
                return;
            }

            int oldXP = currentUser.getExperiencePoints();
            int newXP = oldXP + xPValue;
            //ovde nesto
            boolean isNewLevel = levelAdvancementService.hasReachedNextLevel(newXP, currentUser.getLevel());


            if (userId == currentUser.getUserId()) {
                db.userRepository().updateExperiencePoints(userId, newXP);

                runOnUiThread(() ->
                        Toast.makeText(this, "Dodato " + xPValue + " XP (ukupno: " + newXP + ")", Toast.LENGTH_SHORT).show()
                );
                if(isNewLevel)
                {
                    int newPP = levelAdvancementService.getPPForLevel(currentUser.getLevel());
                    currentUser.setPowerPoints(newPP);
                    db.userRepository().update(currentUser);
                    BattleStartService starter = new BattleStartService(this);
                    BattleStartService.BattleStartResult result = starter.startNewBattle(currentUser);

                    Intent intent = new Intent(this, BattleActivity.class);
                    intent.putExtra("battleId", result.battleId);
                    intent.putExtra("hitChance", result.hitChance);
                    startActivity(intent);
                    currentUser.setNewLevelTime(LocalDateTime.now());
                    db.userRepository().update(currentUser);
                }
            } else {
                runOnUiThread(() ->
                        Toast.makeText(this, "Nije pravilan korisnik!", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void filterTasks(Task.FrequencyEnum frequency) {
        filteredTasks.clear();

        LocalDateTime now = LocalDateTime.now();

        // filtriranje
        for (TaskInstanceWithTask t : allActiveTasks) {
            if (t.task.getFrequency() == frequency) {
                if(t.taskInstance.getEndExecutionTime().plusDays(3).isAfter(now))
                    filteredTasks.add(t);
            }
        }

        // sortiranje po executionTime rastuće
        filteredTasks.sort((t1, t2) -> t1.taskInstance.getStartExecutionTime().compareTo(t2.taskInstance.getStartExecutionTime()));

        // fokusiranje na prvi zadatak koji je danas ili u budućnosti
        int indexToFocus = 0;
        for (int i = 0; i < filteredTasks.size(); i++) {
            if (!filteredTasks.get(i).taskInstance.getStartExecutionTime().isBefore(now)) {
                indexToFocus = i;
                break;
            }
        }

        adapter.notifyDataSetChanged();

        // scroll na fokusirani zadatak
        recyclerView.scrollToPosition(indexToFocus);
    }


}
