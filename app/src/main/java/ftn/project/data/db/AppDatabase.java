package ftn.project.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import ftn.project.domain.entity.Converters;
import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.User;
import ftn.project.domain.repositoryInterface.TaskRepositoryInterface;
import ftn.project.domain.repositoryInterface.UserRepositoryInterface;

@Database(entities = {User.class, Task.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserRepositoryInterface userRepository();
    public abstract TaskRepositoryInterface taskRepository();
    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "habit_baza.db")
                    .addCallback(prepopulateCallback)
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback prepopulateCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);

                    db.execSQL("INSERT INTO User (username, password, experiencePoints, powerPoints, coins) VALUES ('shone','333',20,30,120)");
                    db.execSQL("INSERT INTO User (username, password, experiencePoints, powerPoints, coins) VALUES ('mitar','777',16,32,110)");
                }
            };
}
