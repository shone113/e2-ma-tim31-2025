package ftn.project.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.time.LocalDateTime;

import ftn.project.domain.entity.Task;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "habitquest.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_TASKS = "tasks";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TASKS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userId INTEGER, " +
                "categoryId INTEGER, " +
                "difficulty TEXT, " +
                "importance TEXT, " +
                "frequency TEXT, " +
                "intervalVal INTEGER, " +
                "frequencyUnit TEXT, " +
                "startDate TEXT, " +
                "endDate TEXT, " +
                "name TEXT, " +
                "description TEXT, " +
                "executionTime TEXT, " +
                "status TEXT, " +
                "valueXP INTEGER" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    // INSERT TASK
    public void insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("userId", task.getUserId());
        values.put("categoryId", task.getCategoryId());
        values.put("difficulty", task.getDifficulty().name());
        values.put("importance", task.getImportance().name());
        values.put("frequency", task.getFrequency().name());
        values.put("intervalVal", task.getInterval());
        values.put("frequencyUnit", task.getFrequencyUnit() != null ? task.getFrequencyUnit().name() : null);
        values.put("startDate", task.getStartDate() != null ? task.getStartDate().toString() : null);
        values.put("endDate", task.getEndDate() != null ? task.getEndDate().toString() : null);
        values.put("name", task.getName());
        values.put("description", task.getDescription());
        values.put("executionTime", task.getExecutionTime() != null ? task.getExecutionTime().toString() : null);
        values.put("status", task.getStatus().name());
        values.put("valueXP", task.getValueXP());

        db.insert(TABLE_TASKS, null, values);
        db.close();
    }

    // GET TASK BY ID
    public Task getTask(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Task task = new Task(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("userId")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("categoryId")),
                    Task.DifficultyEnum.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("difficulty"))),
                    Task.ImportanceEnum.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("importance"))),
                    Task.FrequencyEnum.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("frequency"))),
                    cursor.getInt(cursor.getColumnIndexOrThrow("intervalVal")),
                    cursor.getString(cursor.getColumnIndexOrThrow("frequencyUnit")) != null ?
                            Task.FrequencyUnitEnum.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("frequencyUnit"))) : null,
                    cursor.getString(cursor.getColumnIndexOrThrow("startDate")) != null ?
                            LocalDateTime.parse(cursor.getString(cursor.getColumnIndexOrThrow("startDate"))) : null,
                    cursor.getString(cursor.getColumnIndexOrThrow("endDate")) != null ?
                            LocalDateTime.parse(cursor.getString(cursor.getColumnIndexOrThrow("endDate"))) : null,
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getString(cursor.getColumnIndexOrThrow("executionTime")) != null ?
                            LocalDateTime.parse(cursor.getString(cursor.getColumnIndexOrThrow("executionTime"))) : null,
                    Task.TaskStatusEnum.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status")))
            );

            cursor.close();
            db.close();
            return task;
        }

        if (cursor != null) cursor.close();
        db.close();
        return null;
    }
}
