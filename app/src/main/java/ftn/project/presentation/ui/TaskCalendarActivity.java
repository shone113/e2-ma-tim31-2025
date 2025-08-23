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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
import ftn.project.domain.entity.Category;
import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.TaskInstance;
import ftn.project.domain.entity.TaskInstanceWithTask;
import ftn.project.presentation.adapter.HoursAdapter;

public class TaskCalendarActivity extends AppCompatActivity {

    private RecyclerView rvHours;
    private FrameLayout flDaySchedule;
    private FloatingActionButton fabAddTask, fabListTask;
    private TextView tvCurrentDay;
    private ImageButton btnPrevDay, btnNextDay;

    private static final int MINUTE_HEIGHT_DP = 2; // 1 minut = 2dp

    private LocalDate selectedDate = LocalDate.now();
    private final DateTimeFormatter headerFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault());

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
        });

        fabListTask.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskListActivity.class);
            startActivity(intent);
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

        Executors.newSingleThreadExecutor().execute(() -> {
            List<TaskInstanceWithTask> dayTasks = AppDatabase.getInstance(this)
                    .taskInstanceRepository()
                    .getInstancesForDay(dayStart, dayEnd);

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

        btnDone.setOnClickListener(v -> updateStatus(taskInstanceWithTask, TaskInstance.TaskStatusEnum.DONE, tvStatus,
                btnDone, btnCancel, btnPause, btnPlay));
        btnCancel.setOnClickListener(v -> updateStatus(taskInstanceWithTask, TaskInstance.TaskStatusEnum.CANCELED, tvStatus,
                btnDone, btnCancel, btnPause, btnPlay));
        btnPause.setOnClickListener(v -> updateStatus(taskInstanceWithTask, TaskInstance.TaskStatusEnum.PAUSED, tvStatus,
                btnDone, btnCancel, btnPause, btnPlay));
        btnPlay.setOnClickListener(v -> updateStatus(taskInstanceWithTask, TaskInstance.TaskStatusEnum.ACTIVE, tvStatus,
                btnDone, btnCancel, btnPause, btnPlay));
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
