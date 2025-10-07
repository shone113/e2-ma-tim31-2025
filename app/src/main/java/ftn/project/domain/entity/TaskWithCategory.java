package ftn.project.domain.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class TaskWithCategory {
    @Embedded
    public Task task;

    @Relation(
            parentColumn = "categoryId",
            entityColumn = "id"
    )
    public Category category;
}
