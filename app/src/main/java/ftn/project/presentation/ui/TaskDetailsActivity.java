package ftn.project.presentation.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.TaskInstance;
import ftn.project.domain.entity.TaskInstanceWithTask;

public class TaskDetailsActivity extends AppCompatActivity {

    private TextView tvName, tvDescription, tvStatus, tvStartExecutionTime,tvEndExecutionTime,
            tvDifficulty, tvImportance, tvFrequency, tvXP;
    private Button btnDone, btnCanceled, btnPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_details);

        // Inicijalizacija TextView-a
        tvName = findViewById(R.id.tvTaskName);
        tvDescription = findViewById(R.id.tvTaskDescription);
        tvStatus = findViewById(R.id.tvTaskStatus);
        tvStartExecutionTime = findViewById(R.id.tvTaskStartExecutionTime);
        tvEndExecutionTime = findViewById(R.id.tvTaskEndExecutionTime);
        tvDifficulty = findViewById(R.id.tvTaskDifficulty);
        tvImportance = findViewById(R.id.tvTaskImportance);
        tvFrequency = findViewById(R.id.tvTaskFrequency);
        tvXP = findViewById(R.id.tvTaskXP);

        // Inicijalizacija dugmadi
        btnDone = findViewById(R.id.btnDone);
        btnCanceled = findViewById(R.id.btnCanceled);
        btnPaused = findViewById(R.id.btnPaused);

        // Dobijamo taskId iz intent-a
        int taskInstanceId = getIntent().getIntExtra("task_instance_id", -1);

        if (taskInstanceId != -1) {
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                TaskInstanceWithTask taskAndInstance = db.taskInstanceRepository().getTaskInstanceWithTaskById(taskInstanceId); // metoda u DAO

                runOnUiThread(() -> {
                    if (taskAndInstance != null) {
                        // Popunjavanje TextView-a
                        tvName.setText(taskAndInstance.task.getName());
                        tvDescription.setText(taskAndInstance.task.getDescription());
                        tvStatus.setText("Status: " + taskAndInstance.taskInstance.getStatus().name());
                        tvStartExecutionTime.setText("Datum i vreme pocetka zadatka: " +
                                taskAndInstance.taskInstance.getStartExecutionTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                        tvEndExecutionTime.setText("Datum i vreme kraja zadatka: " +
                                taskAndInstance.taskInstance.getEndExecutionTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                        tvDifficulty.setText("Težina: " + taskAndInstance.task.getDifficulty().name());
                        tvImportance.setText("Važnost: " + taskAndInstance.task.getImportance().name());
                        tvFrequency.setText("Tip zadatka: " + taskAndInstance.task.getFrequency().name());
                        tvXP.setText("Vrednost XP: " + taskAndInstance.task.getValueXP());

                        // Klik listeneri za dugmad
                        btnDone.setOnClickListener(v -> updateTaskStatus(taskAndInstance.taskInstance, TaskInstance.TaskStatusEnum.DONE));
                        btnCanceled.setOnClickListener(v -> updateTaskStatus(taskAndInstance.taskInstance, TaskInstance.TaskStatusEnum.CANCELED));
                        btnPaused.setOnClickListener(v -> updateTaskStatus(taskAndInstance.taskInstance, TaskInstance.TaskStatusEnum.PAUSED));
                    }
                });
            });
        }
    }

    // Metoda za ažuriranje statusa zadatka u bazi i osvežavanje prikaza
    private void updateTaskStatus(TaskInstance taskInstance, TaskInstance.TaskStatusEnum newStatus) {
        taskInstance.setStatus(newStatus);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.taskInstanceRepository().updateStatus(taskInstance.getId(), taskInstance.getStatus());
        });

        tvStatus.setText("Status: " + newStatus.name());
    }
}
