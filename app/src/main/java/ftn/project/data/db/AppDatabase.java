package ftn.project.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import ftn.project.domain.entity.Converters;
import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.User;
import ftn.project.domain.repositoryInterface.TaskRepositoryInterface;
import ftn.project.domain.repositoryInterface.UserRepositoryInterface;

@Database(entities = {User.class, Task.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserRepositoryInterface userRepository();
    public abstract TaskRepositoryInterface taskRepository();
    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "habit_quest_baza.db")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }
}
