package ftn.project.presentation.ui;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Task;
import ftn.project.presentation.adapter.TaskAdapter;

public class TaskListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> allTasks = new ArrayList<>();
    private List<Task> filteredTasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list);

        recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(filteredTasks);
        recyclerView.setAdapter(adapter);

        Button btnJednokratni = findViewById(R.id.btnJednokratni);
        Button btnPonavljajuci = findViewById(R.id.btnPonavljajuci);

        loadTasksFromDatabase();

        btnJednokratni.setOnClickListener(v -> filterTasks(Task.FrequencyEnum.ONE_TIME));
        btnPonavljajuci.setOnClickListener(v -> filterTasks(Task.FrequencyEnum.REPEATING));
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
        for (Task t : allTasks) {
            if (t.getFrequency() == frequency) {
                filteredTasks.add(t);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
