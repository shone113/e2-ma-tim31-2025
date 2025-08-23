package ftn.project.presentation.adapter;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import ftn.project.R;
import ftn.project.domain.entity.Category;
import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.TaskInstanceWithTask;

public class DayScheduleAdapter extends RecyclerView.Adapter<DayScheduleAdapter.TaskViewHolder> {

    private List<TaskInstanceWithTask> tasks = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    private static final int MINUTE_HEIGHT_DP = 2;

    public void setTasks(List<TaskInstanceWithTask> tasks, List<Category> categories) {
        tasks.sort((t1, t2) ->
                t1.taskInstance.getStartExecutionTime()
                        .compareTo(t2.taskInstance.getStartExecutionTime())
        );
        this.tasks = tasks;
        this.categories = categories;
        notifyDataSetChanged();
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
        TaskInstanceWithTask item = tasks.get(position);
        Task task = item.task;

        // Naslov taska
        holder.tvTaskTitle.setText(task.getName());

        // Category boja
        Category category = null;
        for (Category c : categories) {
            if (c.getId() == task.getCategoryId()) {
                category = c;
                break;
            }
        }

        // Formatiraj vreme
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String start = item.taskInstance.getStartExecutionTime().format(formatter);
        String end = item.taskInstance.getEndExecutionTime().format(formatter);
        holder.tvStartExecution.setText(start);
        holder.tvEndExecution.setText(end);

        // Trajanje u minutama
        long minutes = Duration.between(
                item.taskInstance.getStartExecutionTime(),
                item.taskInstance.getEndExecutionTime()
        ).toMinutes();

        // Visina task bloka
        int heightPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                minutes * MINUTE_HEIGHT_DP,   // ❌ maknut preuranjen (int)
                holder.itemView.getResources().getDisplayMetrics()
        );

        // Pozicija od ponoći (top margin)
        LocalDateTime startTime = item.taskInstance.getStartExecutionTime();
        long minutesFromMidnight = startTime.getHour() * 60 + startTime.getMinute();

        int marginTopPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                minutesFromMidnight * MINUTE_HEIGHT_DP,   // ❌ maknut preuranjen (int)
                holder.itemView.getResources().getDisplayMetrics()
        );

        // Layout params (FrameLayout da bi mogao slobodno da se pozicionira)
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                heightPx
        );
        lp.topMargin = marginTopPx;
        holder.itemView.setLayoutParams(lp);

        // Boja
        if (category != null) {
            ((CardView) holder.itemView).setCardBackgroundColor(category.getColor());
        } else {
            ((CardView) holder.itemView).setCardBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle, tvStartExecution, tvEndExecution;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvStartExecution = itemView.findViewById(R.id.tvTaskStartExecutionTime);
            tvEndExecution = itemView.findViewById(R.id.tvTaskEndExecutionTime);
        }
    }
}
