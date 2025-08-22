package ftn.project.presentation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.TaskInstance;
import ftn.project.domain.entity.TaskInstanceWithTask;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(TaskInstanceWithTask taskWithInstance);
    }

    private List<TaskInstanceWithTask> taskAndInstanceList;
    private OnTaskClickListener listener;

    public TaskAdapter(List<TaskInstanceWithTask> taskAndInstanceList, OnTaskClickListener listener) {
        this.taskAndInstanceList = taskAndInstanceList;
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
        TaskInstanceWithTask taskInstanceWithTask = taskAndInstanceList.get(position);
        holder.bind(taskInstanceWithTask, listener);  // Adapter samo prosleđuje task
    }

    @Override
    public int getItemCount() {
        return taskAndInstanceList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvStatus, tvStartExecutionTime, tvEndExecutionTime;
        Button btnDone, btnCancel, btnPause, btnPlay;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvStatus = itemView.findViewById(R.id.tvTaskStatus);
            tvStartExecutionTime = itemView.findViewById(R.id.tvTaskStartExecutionTime);
            tvEndExecutionTime = itemView.findViewById(R.id.tvTaskEndExecutionTime);

            btnDone = itemView.findViewById(R.id.btnDone);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnPause = itemView.findViewById(R.id.btnPause);
            btnPlay = itemView.findViewById(R.id.btnPlay);
        }
        public void bind(TaskInstanceWithTask taskInstanceWithTask, TaskAdapter.OnTaskClickListener listener) {
            tvTitle.setText(taskInstanceWithTask.task.getName());
            tvDescription.setText(taskInstanceWithTask.task.getDescription());
            tvStatus.setText("Status: " + taskInstanceWithTask.taskInstance.getStatus().name());

            LocalDateTime startExecutionTime = taskInstanceWithTask.taskInstance.getStartExecutionTime();
            LocalDateTime endExecutionTime = taskInstanceWithTask.taskInstance.getEndExecutionTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            tvStartExecutionTime.setText("Datum i vreme pocetka zadatka: " + startExecutionTime.format(formatter));
            tvEndExecutionTime.setText("Datum i vreme kraja zadatka: " + endExecutionTime.format(formatter));

            itemView.setOnClickListener(v -> listener.onTaskClick(taskInstanceWithTask));
            // Klik listeneri za dugmad
            configureStatusButtons(taskInstanceWithTask);
            
        }
        private void configureStatusButtons(TaskInstanceWithTask taskInstanceWithTask){
            LocalDateTime now = LocalDateTime.now();
            if (taskInstanceWithTask.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.ACTIVE &&
                    taskInstanceWithTask.taskInstance.getEndExecutionTime().plusDays(3).isBefore(now))
            {
                updateStatus(taskInstanceWithTask,TaskInstance.TaskStatusEnum.UNFINISHED);
                btnDone.setEnabled(false);
                btnPause.setEnabled(false);
                btnCancel.setEnabled(false);
                btnPlay.setEnabled(false);
                return;
            }
            if(taskInstanceWithTask.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.CANCELED ||
                    taskInstanceWithTask.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.UNFINISHED)
            {
                btnDone.setEnabled(false);
                btnPause.setEnabled(false);
                btnCancel.setEnabled(false);
                btnPlay.setEnabled(false);
            }
            if(taskInstanceWithTask.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.PAUSED &&
                    taskInstanceWithTask.task.getFrequency() == Task.FrequencyEnum.REPEATING)
            {
                btnDone.setEnabled(false);
                btnCancel.setEnabled(false);
                btnPause.setEnabled(false);
                btnPlay.setEnabled(true);
            }
            if(taskInstanceWithTask.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.ACTIVE)
            {
                if(taskInstanceWithTask.task.getFrequency() == Task.FrequencyEnum.REPEATING)
                    btnPause.setEnabled(true);
                else
                    btnPause.setEnabled(false);
                btnDone.setEnabled(true);
                btnCancel.setEnabled(true);
                btnPlay.setEnabled(false);
            }
            btnDone.setOnClickListener(v -> updateStatus(taskInstanceWithTask, TaskInstance.TaskStatusEnum.DONE));
            btnCancel.setOnClickListener(v -> updateStatus(taskInstanceWithTask, TaskInstance.TaskStatusEnum.CANCELED));
            btnPause.setOnClickListener(v -> updateStatus(taskInstanceWithTask, TaskInstance.TaskStatusEnum.PAUSED));
            btnPlay.setOnClickListener(v -> updateStatus(taskInstanceWithTask, TaskInstance.TaskStatusEnum.ACTIVE));
        }

        private void updateStatus(TaskInstanceWithTask taskInstanceWithTask, TaskInstance.TaskStatusEnum newStatus) {
            taskInstanceWithTask.taskInstance.setStatus(newStatus);
            tvStatus.setText("Status: " + newStatus.name());

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(itemView.getContext());
                db.taskInstanceRepository().updateStatus(taskInstanceWithTask.taskInstance.getId(), taskInstanceWithTask.taskInstance.getStatus());
            });
            configureStatusButtons(taskInstanceWithTask);
        }

    }
}

