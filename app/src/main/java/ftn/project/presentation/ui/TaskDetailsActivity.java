package ftn.project.presentation.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

/*import androidx.appcompat.app.AppCompatActivity;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Task;

public class TaskDetailsActivity extends AppCompatActivity {

    private TextView tvName, tvDescription, tvStatus, tvExecutionTime,
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
        tvExecutionTime = findViewById(R.id.tvTaskExecutionTime);
        tvDifficulty = findViewById(R.id.tvTaskDifficulty);
        tvImportance = findViewById(R.id.tvTaskImportance);
        tvFrequency = findViewById(R.id.tvTaskFrequency);
        tvXP = findViewById(R.id.tvTaskXP);

        // Inicijalizacija dugmadi
        btnDone = findViewById(R.id.btnDone);
        btnCanceled = findViewById(R.id.btnCanceled);
        btnPaused = findViewById(R.id.btnPaused);

        // Dobijamo taskId iz intent-a
        int taskId = getIntent().getIntExtra("task_id", -1);

        if (taskId != -1) {
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                Task task = db.taskRepository().getTaskById(taskId); // metoda u DAO

                runOnUiThread(() -> {
                    if (task != null) {
                        // Popunjavanje TextView-a
                        tvName.setText(task.getName());
                        tvDescription.setText(task.getDescription());
                        tvStatus.setText("Status: " + task.getStatus().name());
                        tvExecutionTime.setText("Datum i vreme izvršavanja: " +
                                task.getExecutionTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                        tvDifficulty.setText("Težina: " + task.getDifficulty().name());
                        tvImportance.setText("Važnost: " + task.getImportance().name());
                        tvFrequency.setText("Tip zadatka: " + task.getFrequency().name());
                        tvXP.setText("Vrednost XP: " + task.getValueXP());

                        // Klik listeneri za dugmad
                        btnDone.setOnClickListener(v -> updateTaskStatus(task, Task.TaskStatusEnum.DONE));
                        btnCanceled.setOnClickListener(v -> updateTaskStatus(task, Task.TaskStatusEnum.CANCELED));
                        btnPaused.setOnClickListener(v -> updateTaskStatus(task, Task.TaskStatusEnum.PAUSED));
                    }
                });
            });
        }
    }

    // Metoda za ažuriranje statusa zadatka u bazi i osvežavanje prikaza
    private void updateTaskStatus(Task task, Task.TaskStatusEnum newStatus) {
        task.setStatus(newStatus);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.taskRepository().updateTaskStatus(task.getId(), task.getStatus());
        });

        tvStatus.setText("Status: " + newStatus.name());
    }
}
*/