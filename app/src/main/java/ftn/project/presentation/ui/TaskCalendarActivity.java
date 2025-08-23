package ftn.project.presentation.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
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
import java.util.List;
import java.util.concurrent.Executors;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Category;
import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.TaskInstanceWithTask;
import ftn.project.presentation.adapter.HoursAdapter;

public class TaskCalendarActivity extends AppCompatActivity {

    private RecyclerView rvWeekDays, rvHours;
    private FrameLayout flDaySchedule;
    private FloatingActionButton fabAddTask;

    private static final int MINUTE_HEIGHT_DP = 2; // 1 minut = 2dp

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_calendar);

        flDaySchedule = findViewById(R.id.flDaySchedule);
        rvWeekDays = findViewById(R.id.rvWeekDays);
        rvHours = findViewById(R.id.rvHours);
        fabAddTask = findViewById(R.id.fabAddTask);

        // Satnica levo
        rvHours.setLayoutManager(new LinearLayoutManager(this));
        rvHours.setAdapter(new HoursAdapter());

// bitno: reci mu da zauzme celu visinu dana
        rvHours.getLayoutParams().height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                24 * 60 * MINUTE_HEIGHT_DP, // 24h u minutima * visina minuta
                getResources().getDisplayMetrics());
        // Učitaj taskove za danas
        loadTasksForToday();

        // Horizontalni week picker
        rvWeekDays.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        fabAddTask.setOnClickListener(v -> {
            // logika za dodavanje novog zadatka
        });
    }

    private void loadTasksForToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

        Executors.newSingleThreadExecutor().execute(() -> {
            List<TaskInstanceWithTask> todayTasks = AppDatabase.getInstance(this)
                    .taskInstanceRepository()
                    .getInstancesForDay(dayStart, dayEnd);

            List<Category> categories = AppDatabase.getInstance(this)
                    .categoryRepository()
                    .getAll();

            runOnUiThread(() -> renderTasks(todayTasks, categories));
        });
    }

    private void renderTasks(List<TaskInstanceWithTask> tasks, List<Category> categories) {
        flDaySchedule.removeAllViews();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        for (TaskInstanceWithTask item : tasks) {
            Task task = item.task;

            // Inflate task_item.xml (CardView kao jedan task blok)
            CardView taskView = (CardView) LayoutInflater.from(this)
                    .inflate(R.layout.task_item, flDaySchedule, false);

            TextView tvTaskTitle = taskView.findViewById(R.id.tvTaskTitle);
            TextView tvStartExecution = taskView.findViewById(R.id.tvTaskStartExecutionTime);
            TextView tvEndExecution = taskView.findViewById(R.id.tvTaskEndExecutionTime);

            tvTaskTitle.setText(task.getName());

            String start = item.taskInstance.getStartExecutionTime().format(formatter);
            String end = item.taskInstance.getEndExecutionTime().format(formatter);
            tvStartExecution.setText(start);
            tvEndExecution.setText(end);

            // Trajanje taska
            long minutes = Duration.between(
                    item.taskInstance.getStartExecutionTime(),
                    item.taskInstance.getEndExecutionTime()
            ).toMinutes();

            int heightPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    (int) (minutes * MINUTE_HEIGHT_DP),
                    getResources().getDisplayMetrics()
            );

            // Pozicija od ponoći
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

            // boja kategorije
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

            flDaySchedule.addView(taskView);
        }
    }
}
