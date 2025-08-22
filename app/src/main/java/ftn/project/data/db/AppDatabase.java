package ftn.project.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ftn.project.domain.entity.Converters;
import ftn.project.domain.entity.Equipment;
import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.TaskInstance;
import ftn.project.domain.entity.User;
import ftn.project.domain.repositoryInterface.EquipmentRepositoryInterface;
import ftn.project.domain.repositoryInterface.TaskInstanceRepositoryInterface;
import ftn.project.domain.repositoryInterface.TaskRepositoryInterface;
import ftn.project.domain.repositoryInterface.UserRepositoryInterface;


@Database(entities = {User.class, Task.class, TaskInstance.class, Equipment.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserRepositoryInterface userRepository();
    public abstract TaskRepositoryInterface taskRepository();
    public abstract TaskInstanceRepositoryInterface taskInstanceRepository();
    public abstract EquipmentRepositoryInterface equipmentRepository();
    private static AppDatabase INSTANCE;
    private static Context appContext;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
            // primer: db.execSQL("ALTER TABLE Equipment ADD COLUMN description TEXT");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
            // primer: db.execSQL("ALTER TABLE Equipment ADD COLUMN description TEXT");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            appContext = context.getApplicationContext();
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                     AppDatabase.class, "habit_quest_baza8.db")
                    //.addMigrations(MIGRATION_1_2)
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
                    loadSqlFromAssets(appContext, db, "sql/equipment.sql");
                }
            };

    private static void loadSqlFromAssets(Context appContext, SupportSQLiteDatabase db, String assetPath) {
        try (InputStream is = appContext.getAssets().open(assetPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            StringBuilder stmt = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) continue;
                stmt.append(line);
                // ako se komanda završava sa ;
                if (line.endsWith(";")) {
                    db.execSQL(stmt.substring(0, stmt.length() - 1)); // ukloni ;
                    stmt.setLength(0);
                } else {
                    stmt.append(' ');
                }
            }
            // poslednja komanda ako nema ;
            if (stmt.length() > 0) {
                db.execSQL(stmt.toString().trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
