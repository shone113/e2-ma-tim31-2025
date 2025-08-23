package ftn.project.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Category;
import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.TaskInstance;
import ftn.project.domain.entity.TaskInstanceWithTask;

public class TaskDetailsActivity extends AppCompatActivity {

    private TextView tvName, tvDescription, tvStatus, tvStartExecutionTime, tvEndExecutionTime,
            tvDifficulty, tvImportance, tvFrequency, tvXP, tvTaskCategory;
    private Button btnDone, btnCanceled, btnPaused, btnUpdateTask, btnDeleteTask;

    private int taskInstanceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_details);

        tvName = findViewById(R.id.tvTaskName);
        tvDescription = findViewById(R.id.tvTaskDescription);
        tvStatus = findViewById(R.id.tvTaskStatus);
        tvStartExecutionTime = findViewById(R.id.tvTaskStartExecutionTime);
        tvEndExecutionTime = findViewById(R.id.tvTaskEndExecutionTime);
        tvDifficulty = findViewById(R.id.tvTaskDifficulty);
        tvImportance = findViewById(R.id.tvTaskImportance);
        tvFrequency = findViewById(R.id.tvTaskFrequency);
        tvXP = findViewById(R.id.tvTaskXP);
        tvTaskCategory = findViewById(R.id.tvTaskCategory);

        btnDone = findViewById(R.id.btnDone);
        btnCanceled = findViewById(R.id.btnCanceled);
        btnPaused = findViewById(R.id.btnPaused);
        btnUpdateTask = findViewById(R.id.btnUpdateTask);
        btnDeleteTask = findViewById(R.id.btnDeleteTask);

        // Čuvamo taskInstanceId
        taskInstanceId = getIntent().getIntExtra("task_instance_id", -1);

        btnUpdateTask.setOnClickListener(v -> {
            Intent intent = new Intent(TaskDetailsActivity.this, TaskEditActivity.class);
            intent.putExtra("task_instance_id", taskInstanceId);
            startActivity(intent);
        });


        if (taskInstanceId != -1) {
            loadTaskDetails(taskInstanceId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (taskInstanceId != -1) {
            loadTaskDetails(taskInstanceId);

        }
    }

    private void loadTaskDetails(int taskInstanceId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            TaskInstanceWithTask taskAndInstance = db.taskInstanceRepository().getTaskInstanceWithTaskById(taskInstanceId);
            int categoryId = taskAndInstance.task.getCategoryId();
            Category category = db.categoryRepository().getCategory(categoryId);
            runOnUiThread(() -> {
                if (taskAndInstance != null) {
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
                    tvTaskCategory.setText("Kategorija: "+ category.getName());
                    configureUpdateButton(taskAndInstance);
                    configureDeleteButton(taskAndInstance);
                    configureStatusButtons(taskAndInstance);
                }
            });
        });
    }

    private void configureUpdateButton(TaskInstanceWithTask taskAndInstance) {
        TaskInstance.TaskStatusEnum status = taskAndInstance.taskInstance.getStatus();
        LocalDateTime now = LocalDateTime.now();

        if (status == TaskInstance.TaskStatusEnum.DONE || taskAndInstance.taskInstance.getEndExecutionTime().isBefore(now)) {
            btnUpdateTask.setEnabled(false);
        } else if (taskAndInstance.task.getFrequency() == Task.FrequencyEnum.REPEATING &&
                ((now.isBefore(taskAndInstance.taskInstance.getEndExecutionTime()) &&
                        now.isAfter(taskAndInstance.taskInstance.getStartExecutionTime()))
                        || taskAndInstance.taskInstance.getEndExecutionTime().isBefore(now))) {
            btnUpdateTask.setEnabled(false);
        } else {
            btnUpdateTask.setEnabled(true);
        }
    }

    private void configureStatusButtons(TaskInstanceWithTask taskAndInstance) {
        LocalDateTime now = LocalDateTime.now();

        if (taskAndInstance.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.ACTIVE &&
                taskAndInstance.taskInstance.getEndExecutionTime().plusDays(3).isBefore(now))
        {
            updateTaskStatus(taskAndInstance,TaskInstance.TaskStatusEnum.UNFINISHED);
            btnDone.setEnabled(false);
            btnPaused.setEnabled(false);
            btnCanceled.setEnabled(false);
            btnDeleteTask.setEnabled(false);
            btnUpdateTask.setEnabled(false);
            Toast.makeText(this, "Zadatak je istekao i označen kao neurađen", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!taskAndInstance.taskInstance.getStartExecutionTime().plusDays(3).isBefore(now)) {
            if (taskAndInstance.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.CANCELED ||
                    taskAndInstance.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.UNFINISHED ||
                    taskAndInstance.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.DONE) {
                btnDone.setEnabled(false);
                btnPaused.setEnabled(false);
                btnCanceled.setEnabled(false);
                btnDeleteTask.setEnabled(false);
                btnUpdateTask.setEnabled(false);
            }
            if (taskAndInstance.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.PAUSED &&
                    taskAndInstance.task.getFrequency() == Task.FrequencyEnum.REPEATING) {
                btnDone.setEnabled(false);
                btnCanceled.setEnabled(false);
                btnDeleteTask.setEnabled(false);
                btnUpdateTask.setEnabled(true);

                btnPaused.setText("Aktiviraj");
                btnPaused.setEnabled(true);
                //btnActive.setEnabled(true);
                btnPaused.setOnClickListener(v -> updateTaskStatus(taskAndInstance, TaskInstance.TaskStatusEnum.ACTIVE));
            } else {
                btnPaused.setText("Pauziran");
                btnPaused.setOnClickListener(v -> updateTaskStatus(taskAndInstance, TaskInstance.TaskStatusEnum.PAUSED));
            }
            if (taskAndInstance.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.ACTIVE) {
                if (taskAndInstance.task.getFrequency() == Task.FrequencyEnum.REPEATING)
                    btnPaused.setEnabled(true);
                else
                    btnPaused.setEnabled(false);
                btnDone.setEnabled(true);
                btnCanceled.setEnabled(true);
                btnDeleteTask.setEnabled(true);
                btnUpdateTask.setEnabled(true);
            }
        }
        else
        {
            btnDeleteTask.setEnabled(false);
            btnUpdateTask.setEnabled(false);
        }

        btnDone.setOnClickListener(v -> updateTaskStatus(taskAndInstance, TaskInstance.TaskStatusEnum.DONE));
        btnCanceled.setOnClickListener(v -> updateTaskStatus(taskAndInstance, TaskInstance.TaskStatusEnum.CANCELED));
    }

    private void updateTaskStatus(TaskInstanceWithTask taskAndInstance, TaskInstance.TaskStatusEnum newStatus) {
        taskAndInstance.taskInstance.setStatus(newStatus);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.taskInstanceRepository().updateStatus(taskAndInstance.taskInstance.getId(), taskAndInstance.taskInstance.getStatus());
        });

        runOnUiThread(() -> {
            tvStatus.setText("Status: " + newStatus.name());
            Toast.makeText(this, "Status uspešno ažuriran!", Toast.LENGTH_SHORT).show();
            configureUpdateButton(taskAndInstance);
            configureDeleteButton(taskAndInstance);
            configureStatusButtons(taskAndInstance);
        });
    }
    private void configureDeleteButton(TaskInstanceWithTask taskAndInstance) {
        TaskInstance.TaskStatusEnum status = taskAndInstance.taskInstance.getStatus();
        LocalDateTime now = LocalDateTime.now();
        if(status == TaskInstance.TaskStatusEnum.DONE || now.isAfter(taskAndInstance.taskInstance.getEndExecutionTime()))
            btnDeleteTask.setEnabled(false);
        else
        {
            btnDeleteTask.setEnabled(true);
            btnDeleteTask.setOnClickListener(v -> {
                deleteTaskAndInstances(taskAndInstance);
            });
        }
    }
    private void deleteTaskAndInstances(TaskInstanceWithTask taskAndInstance) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            Task task = taskAndInstance.task;
            TaskInstance instance = taskAndInstance.taskInstance;

            LocalDateTime now = LocalDateTime.now();

            // ovo cisto radi preventive, necemo ni dopustiti da bude enable dugme za brisanje
            if (instance.getStatus() != TaskInstance.TaskStatusEnum.ACTIVE
                    || instance.getStartExecutionTime().isBefore(now)) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Nije moguće obrisati završen zadatak!", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            boolean isAnyInstanceCompleted = db.taskInstanceRepository().hasLockedInstances(task.getId(),now);
            // ovo vazi za jednokratne, tu brisemo i instancu i task
            if (task.getFrequency() == Task.FrequencyEnum.ONE_TIME && !isAnyInstanceCompleted) {
                db.taskInstanceRepository().deleteByTaskId(task.getId());
                db.taskRepository().delete(task);
            }

            // ponavljajuci, ako imamo neku instancu taska koja je izvrsena, onda ne smemo brisati task
            //jer i dalje postoji instanca taska u tabeli, posto je receno da se prethondno izvrseni ponavljajuci
            //zadaci i dalje prikazuju i ne brisu, pa ce se samo obrisati instance tog taska koje jos nisu izvrsene
            else if (task.getFrequency() == Task.FrequencyEnum.REPEATING) {

                if(isAnyInstanceCompleted)
                {
                    db.taskInstanceRepository().deleteFutureInstances(task.getId(), now);
                }
                else
                {
                    db.taskInstanceRepository().deleteFutureInstances(task.getId(), now);
                    db.taskRepository().delete(task);
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Zadatak obrisan!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

}
