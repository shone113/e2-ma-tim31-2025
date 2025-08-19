package ftn.project.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import ftn.project.domain.entity.User;
import ftn.project.domain.repositoryInterface.UserRepositoryInterface;

@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserRepositoryInterface userRepository();
    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "habit_quest.db")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }
}
