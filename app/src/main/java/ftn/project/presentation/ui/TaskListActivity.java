package ftn.project.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.time.LocalDateTime;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Task;
import ftn.project.presentation.adapter.TaskAdapter;

public class TaskListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> allTasks = new ArrayList<>();
    private List<Task> filteredTasks = new ArrayList<>();
    private ActivityResultLauncher<Intent> taskDetailsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list);

        recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(filteredTasks, task -> {
            // Kada se klikne na task
            Intent intent = new Intent(TaskListActivity.this, TaskDetailsActivity.class);
            intent.putExtra("task_id", task.getId()); // prosleđuješ ID taska
            startActivity(intent);
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
        loadTasksFromDatabase();
    }

    private void loadTasksFromDatabase() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            allTasks = db.taskRepository().getAllTasks();

            runOnUiThread(() -> {
                filteredTasks.clear();
                filteredTasks.addAll(allTasks);
                adapter.notifyDataSetChanged();
            });
        });
    }


    private void filterTasks(Task.FrequencyEnum frequency) {
        filteredTasks.clear();

        LocalDateTime now = LocalDateTime.now();

        // filtriranje
        for (Task t : allTasks) {
            if (t.getFrequency() == frequency) {
                filteredTasks.add(t);
            }
        }

        // sortiranje po executionTime rastuće
        filteredTasks.sort((t1, t2) -> t1.getExecutionTime().compareTo(t2.getExecutionTime()));

        // fokusiranje na prvi zadatak koji je danas ili u budućnosti
        int indexToFocus = 0;
        for (int i = 0; i < filteredTasks.size(); i++) {
            if (!filteredTasks.get(i).getExecutionTime().isBefore(now)) {
                indexToFocus = i;
                break;
            }
        }

        adapter.notifyDataSetChanged();

        // scroll na fokusirani zadatak
        recyclerView.scrollToPosition(indexToFocus);
    }


}
