/*package ftn.project.presentation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Task;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    private List<Task> taskList;
    private OnTaskClickListener listener;

    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task, listener);  // Adapter samo prosleđuje task
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvStatus, tvExexutionTime;
        Button btnDone, btnCancel, btnPause, btnPlay;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvStatus = itemView.findViewById(R.id.tvTaskStatus);
            tvExexutionTime = itemView.findViewById(R.id.tvTaskExecutionTime);

            btnDone = itemView.findViewById(R.id.btnDone);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnPause = itemView.findViewById(R.id.btnPause);
            btnPlay = itemView.findViewById(R.id.btnPlay);
        }
        public void bind(Task task, TaskAdapter.OnTaskClickListener listener) {
            tvTitle.setText(task.getName());
            tvDescription.setText(task.getDescription());
            tvStatus.setText("Status: " + task.getStatus().name());

            LocalDateTime executionTime = task.getExecutionTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            tvExexutionTime.setText("Datum i vreme izvršavanja: " + executionTime.format(formatter));

            itemView.setOnClickListener(v -> listener.onTaskClick(task));
            // Klik listeneri za dugmad
            btnDone.setOnClickListener(v -> updateStatus(task, Task.TaskStatusEnum.DONE));
            btnCancel.setOnClickListener(v -> updateStatus(task, Task.TaskStatusEnum.CANCELED));
            btnPause.setOnClickListener(v -> updateStatus(task, Task.TaskStatusEnum.PAUSED));
            btnPlay.setOnClickListener(v -> updateStatus(task, Task.TaskStatusEnum.ACTIVE));

            // Boja dugmadi po statusu
            updateButtonColors(task.getStatus());
        }

        private void updateStatus(Task task, Task.TaskStatusEnum newStatus) {
            task.setStatus(newStatus);
            tvStatus.setText("Status: " + newStatus.name());

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(itemView.getContext());
                db.taskRepository().updateTaskStatus(task.getId(), task.getStatus());
            });

            updateButtonColors(newStatus);
        }

        private void updateButtonColors(Task.TaskStatusEnum status) {
            btnDone.setBackgroundColor(0xFFCCCCCC);
            btnCancel.setBackgroundColor(0xFFCCCCCC);
            btnPause.setBackgroundColor(0xFFCCCCCC);
            btnPlay.setBackgroundColor(0xFFCCCCCC);

            switch (status) {
                case DONE: btnDone.setBackgroundColor(0xFF4CAF50); break;
                case CANCELED: btnCancel.setBackgroundColor(0xFFF44336); break;
                case PAUSED: btnPause.setBackgroundColor(0xFFFFC107); break;
                case ACTIVE: btnPlay.setBackgroundColor(0xFF2196F3); break;
            }
        }

    }
}
*/