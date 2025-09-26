package ftn.project.presentation.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Alliance;
import ftn.project.domain.entity.Battle;
import ftn.project.domain.entity.Boss;
import ftn.project.domain.entity.Category;
import ftn.project.domain.entity.SpecialMission;
import ftn.project.domain.entity.SpecialMissionProgress;
import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.TaskInstance;
import ftn.project.domain.entity.TaskInstanceWithTask;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.BattleStartService;
import ftn.project.domain.usecase.BossService;
import ftn.project.domain.usecase.CheckQuotaService;
import ftn.project.domain.usecase.LevelAdvancementService;
import ftn.project.domain.usecase.LoggedUserService;
import ftn.project.domain.usecase.QuotaFinalizer;
import ftn.project.domain.usecase.SpecialMissionProgressService;
import ftn.project.domain.usecase.SuccessRateService;
import ftn.project.presentation.adapter.HoursAdapter;

public class TaskCalendarActivity extends AppCompatActivity {

    private RecyclerView rvHours;
    private FrameLayout flDaySchedule;
    private FloatingActionButton fabAddTask, fabListTask, fabBattle, fabAlliance;
    private TextView tvCurrentDay;
    private ImageButton btnPrevDay, btnNextDay;

    private static final int MINUTE_HEIGHT_DP = 2; // 1 minut = 2dp

    private LocalDate selectedDate = LocalDate.now();
    private final DateTimeFormatter headerFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault());
    private SpecialMissionProgressService smps;
    private LevelAdvancementService levelAdvancementService;

    @Override
    protected void onResume() {
        super.onResume();
        loadTasksForDate(selectedDate);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_calendar);

        flDaySchedule = findViewById(R.id.flDaySchedule);
        rvHours = findViewById(R.id.rvHours);
        fabAddTask = findViewById(R.id.fabAddTask);
        fabListTask = findViewById(R.id.fabListTasks);
        fabBattle = findViewById(R.id.fabBattle);
        fabAlliance = findViewById(R.id.fabAlliance);
        tvCurrentDay = findViewById(R.id.tvCurrentWeek);
        btnPrevDay = findViewById(R.id.btnPrevDay);
        btnNextDay = findViewById(R.id.btnNextDay);

        rvHours.setLayoutManager(new LinearLayoutManager(this));
        rvHours.setAdapter(new HoursAdapter());

        rvHours.getLayoutParams().height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                24 * 60 * MINUTE_HEIGHT_DP,
                getResources().getDisplayMetrics());

        updateHeader();
        loadTasksForDate(selectedDate);

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(TaskCalendarActivity.this, NewTaskActivity.class);
            startActivity(intent);

            //PROBA
            /*
            AppDatabase db = AppDatabase.getInstance(this);
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            String firebaseUid = firebaseUser.getUid();
            User currentUser = db.userRepository().getByFirebaseUid(firebaseUid);
            db.userRepository().updateLevel(currentUser.getUserId(),0);
            db.battleRepository().deleteAll();
            db.bossRepository().deleteAll();*/
        });

        fabListTask.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskListActivity.class);
            startActivity(intent);
        });

        fabBattle.setOnClickListener(v -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            String firebaseUid = firebaseUser.getUid();

            AppDatabase db = AppDatabase.getInstance(this);
            User currentUser = db.userRepository().getByFirebaseUid(firebaseUid);

            BattleStartService starter = new BattleStartService(this);
            BattleStartService.BattleStartResult result = starter.startNewBattle(currentUser);

            Intent intent = new Intent(this, BattleActivity.class);
            intent.putExtra("battleId", result.battleId);
            intent.putExtra("hitChance", result.hitChance);
            startActivity(intent);
        });
        fabAlliance.setOnClickListener(v ->{
            AppDatabase db = AppDatabase.getInstance(this);
            Alliance alliance = new Alliance();
            alliance.setAllianceId(1);
            alliance.setName("Alijansa");
            alliance.setLeaderUserId(1027);
            db.allianceRepository().insert(alliance);

            User u1 = db.userRepository().getById(1027);
            User u2 = db.userRepository().getById(1028);
            u2.setAllianceId(1);
            u1.setAllianceId(1);
            db.userRepository().update(u2);
            db.userRepository().update(u1);
        });


        btnPrevDay.setOnClickListener(v -> {
            selectedDate = selectedDate.minusDays(1);
            updateHeader();
            loadTasksForDate(selectedDate);
        });

        btnNextDay.setOnClickListener(v -> {
            selectedDate = selectedDate.plusDays(1);
            updateHeader();
            loadTasksForDate(selectedDate);
        });

        tvCurrentDay.setOnClickListener(v -> openDatePicker());
    }

    private void updateHeader() {
        tvCurrentDay.setText(headerFormatter.format(selectedDate));
    }

    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth());

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    updateHeader();
                    loadTasksForDate(selectedDate);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void loadTasksForDate(LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        LoggedUserService loggedService = new LoggedUserService(this);
        User user = loggedService.getCurrentUser();
        Executors.newSingleThreadExecutor().execute(() -> {

            List<TaskInstanceWithTask> dayTasks = AppDatabase.getInstance(this)
                    .taskInstanceRepository()
                    .getInstancesForDay(user.getUserId(),dayStart, dayEnd);

            List<Category> categories = AppDatabase.getInstance(this)
                    .categoryRepository()
                    .getAll();

            runOnUiThread(() -> renderTasks(dayTasks, categories));
        });
    }

    private void renderTasks(List<TaskInstanceWithTask> tasks, List<Category> categories) {
        flDaySchedule.removeAllViews();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        for (TaskInstanceWithTask item : tasks) {
            Task task = item.task;

            CardView taskView = (CardView) LayoutInflater.from(this)
                    .inflate(R.layout.task_item, flDaySchedule, false);

            TextView tvTaskTitle = taskView.findViewById(R.id.tvTaskTitle);
            TextView tvStartExecution = taskView.findViewById(R.id.tvTaskStartExecutionTime);
            TextView tvEndExecution = taskView.findViewById(R.id.tvTaskEndExecutionTime);
            TextView tvStatus = taskView.findViewById(R.id.tvTaskStatus);

            Button btnDone = taskView.findViewById(R.id.btnDone);
            Button btnCancel = taskView.findViewById(R.id.btnCancel);
            Button btnPause = taskView.findViewById(R.id.btnPause);
            Button btnPlay = taskView.findViewById(R.id.btnPlay);

            tvTaskTitle.setText(task.getName());

            String start = item.taskInstance.getStartExecutionTime().format(formatter);
            String end = item.taskInstance.getEndExecutionTime().format(formatter);
            tvStartExecution.setText(start);
            tvEndExecution.setText(end);
            tvStatus.setText("Status: " + item.taskInstance.getStatus().name());

            configureStatusButtons(item, tvStatus, btnDone, btnCancel, btnPause, btnPlay);

            // Trajanje i pozicija
            long minutes = Duration.between(
                    item.taskInstance.getStartExecutionTime(),
                    item.taskInstance.getEndExecutionTime()
            ).toMinutes();

            int heightPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    (int) (minutes * MINUTE_HEIGHT_DP),
                    getResources().getDisplayMetrics()
            );

            LocalDateTime startTime = item.taskInstance.getStartExecutionTime();
            long minutesFromMidnight = startTime.getHour() * 60 + startTime.getMinute();

            int marginTopPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    (int) (minutesFromMidnight * MINUTE_HEIGHT_DP),
                    getResources().getDisplayMetrics()
            );

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    heightPx
            );
            lp.topMargin = marginTopPx;

            taskView.setLayoutParams(lp);

            Category category = null;
            for (Category c : categories) {
                if (c.getId() == task.getCategoryId()) {
                    category = c;
                    break;
                }
            }
            if (category != null) {
                taskView.setCardBackgroundColor(category.getColor());
            } else {
                taskView.setCardBackgroundColor(Color.GRAY);
            }

            // klik otvara detalje
            taskView.setOnClickListener(v -> {
                Intent intent = new Intent(this, TaskDetailsActivity.class);
                intent.putExtra("task_instance_id", item.taskInstance.getId());
                startActivity(intent);
            });

            flDaySchedule.addView(taskView);
        }
    }

    private void configureStatusButtons(TaskInstanceWithTask taskInstanceWithTask,
                                        TextView tvStatus,
                                        Button btnDone,
                                        Button btnCancel,
                                        Button btnPause,
                                        Button btnPlay) {

        LocalDateTime now = LocalDateTime.now();

        if (taskInstanceWithTask.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.ACTIVE &&
                taskInstanceWithTask.taskInstance.getEndExecutionTime().plusDays(3).isBefore(now)) {
            updateStatus(taskInstanceWithTask, TaskInstance.TaskStatusEnum.UNFINISHED, tvStatus,
                    btnDone, btnCancel, btnPause, btnPlay);
            disableAll(btnDone, btnCancel, btnPause, btnPlay);
            return;
        }

        if (taskInstanceWithTask.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.CANCELED ||
                taskInstanceWithTask.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.UNFINISHED||
                taskInstanceWithTask.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.DONE) {
            disableAll(btnDone, btnCancel, btnPause, btnPlay);
        }

        if (taskInstanceWithTask.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.PAUSED &&
                taskInstanceWithTask.task.getFrequency() == Task.FrequencyEnum.REPEATING) {
            disableAll(btnDone, btnCancel, btnPause);
            btnPlay.setEnabled(true);
        }

        if (taskInstanceWithTask.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.ACTIVE) {
            btnDone.setEnabled(true);
            btnCancel.setEnabled(true);
            btnPlay.setEnabled(false);

            if (taskInstanceWithTask.task.getFrequency() == Task.FrequencyEnum.REPEATING) {
                btnPause.setEnabled(true);
            } else {
                btnPause.setEnabled(false);
            }
        }

        btnDone.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                LoggedUserService loggedService = new LoggedUserService(this);
                User user = loggedService.getCurrentUser();
                smps = new SpecialMissionProgressService(
                        db.specialMissionRepository(),
                        db.specialMissionProgressRepository(),
                        db.taskInstanceRepository(),
                        db.allianceMessageRepository()
                );

                // ✅ Izračunaj XP na osnovu kvota
                int earnedXp = CheckQuotaService.calculateEarnedXP(taskInstanceWithTask.taskInstance,user.getUserId(),user.getLevel(), db);

                // ✅ Postavi status na DONE i upiši XP u model
                taskInstanceWithTask.taskInstance.setStatus(TaskInstance.TaskStatusEnum.DONE);
                taskInstanceWithTask.taskInstance.setEarnedXp(earnedXp);

                // ✅ Update baze: status + XP + withinQuota flag
                db.taskInstanceRepository().updateStatus(
                        taskInstanceWithTask.taskInstance.getId(),
                        TaskInstance.TaskStatusEnum.DONE
                );
                db.taskInstanceRepository().updateEarnedXpAndQuota(
                        taskInstanceWithTask.taskInstance.getId(),
                        earnedXp,
                        taskInstanceWithTask.taskInstance.isWithinQuota()
                );

                // ✅ Ako ima XP, dodaj korisniku
                if (earnedXp > 0) {
                    updateLoggedUserPoints(taskInstanceWithTask.task.getUserId(), earnedXp);
                }
                int increment = smps.punchByEasyTaskIncrement(user.getUserId(), taskInstanceWithTask.taskInstance);
                int incrementHard = smps.punchByHardTaskIncerement(user.getUserId(), taskInstanceWithTask.taskInstance);
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

                // ✅ Refresh UI odmah
                runOnUiThread(() -> {
                    tvStatus.setText("Status: DONE");
                    Toast.makeText(this, "Zadatak završen! Dobio si " + earnedXp + " XP", Toast.LENGTH_SHORT).show();

                    configureStatusButtons(taskInstanceWithTask, tvStatus, btnDone, btnCancel, btnPause, btnPlay);
                });
            });
        });

        btnCancel.setOnClickListener(v -> updateStatus(taskInstanceWithTask, TaskInstance.TaskStatusEnum.CANCELED, tvStatus,
                btnDone, btnCancel, btnPause, btnPlay));
        btnPause.setOnClickListener(v -> updateStatus(taskInstanceWithTask, TaskInstance.TaskStatusEnum.PAUSED, tvStatus,
                btnDone, btnCancel, btnPause, btnPlay));
        btnPlay.setOnClickListener(v -> updateStatus(taskInstanceWithTask, TaskInstance.TaskStatusEnum.ACTIVE, tvStatus,
                btnDone, btnCancel, btnPause, btnPlay));
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
            levelAdvancementService = new LevelAdvancementService(db.levelRepository());
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



    private void updateStatus(TaskInstanceWithTask taskInstanceWithTask,
                              TaskInstance.TaskStatusEnum newStatus,
                              TextView tvStatus,
                              Button btnDone,
                              Button btnCancel,
                              Button btnPause,
                              Button btnPlay) {

        taskInstanceWithTask.taskInstance.setStatus(newStatus);
        tvStatus.setText("Status: " + newStatus.name());

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.taskInstanceRepository().updateStatus(
                    taskInstanceWithTask.taskInstance.getId(),
                    taskInstanceWithTask.taskInstance.getStatus()
            );
        });

        configureStatusButtons(taskInstanceWithTask, tvStatus, btnDone, btnCancel, btnPause, btnPlay);
    }

    private void disableAll(Button... buttons) {
        for (Button b : buttons) {
            b.setEnabled(false);
        }
    }
}
