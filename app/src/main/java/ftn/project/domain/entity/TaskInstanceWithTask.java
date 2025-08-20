package ftn.project.domain.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class TaskInstanceWithTask {
    @Embedded
    public TaskInstance taskInstance;

    @Relation(
            parentColumn = "taskId",
            entityColumn = "id"
    )
    public Task task;
}
