package ftn.project.presentation.ui;
/*Ukratko: UI elementi u Androidu mogu da se menjaju samo sa glavne (UI) niti.

Kad ti koristiš Executors.newSingleThreadExecutor(), kod unutar njega se izvršava u pozadinskoj niti (background thread). Ako pokušaš direktno da pozoveš setText, setChecked ili slično, Android baca grešku CalledFromWrongThreadException.

runOnUiThread prebacuje taj kod na glavnu nit, pa je bezbedno menjati TextView, Button, TimePicker i sve ostale UI komponente.

Jednostavno: background thread radi posao (npr. čitanje iz baze), runOnUiThread ažurira ekran.*/
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.TaskInstance;
import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.TaskInstanceWithTask;

public class TaskEditActivity extends AppCompatActivity {
    private EditText etName, etDescription;
    private TimePicker timePickerStart, timePickerEnd;
    private RadioGroup rgDifficulty, rgImportance;
    private RadioButton rbVeomaLak, rbLak, rbTezak, rbEkstremnoTezak;
    private RadioButton rbNormalan, rbVazan, rbEkstremnoVazan, rbSpecijalan;
    private Button btnSaveChanges;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_task);

        etName = findViewById(R.id.etNaziv);
        etDescription = findViewById(R.id.etOpis);
        timePickerStart = findViewById(R.id.timePickerStart);
        timePickerEnd = findViewById(R.id.timePickerEnd);
        rgDifficulty = findViewById(R.id.rgTezina);
        rgImportance = findViewById(R.id.rgBitnost);
        rbVeomaLak = findViewById(R.id.rbVeomaLak);
        rbLak = findViewById(R.id.rbLak);
        rbTezak = findViewById(R.id.rbTezak);
        rbEkstremnoTezak = findViewById(R.id.rbEkstremnoTezak);
        rbNormalan = findViewById(R.id.rbNormalan);
        rbVazan = findViewById(R.id.rbVazan);
        rbEkstremnoVazan = findViewById(R.id.rbEkstremnoVazan);
        rbSpecijalan = findViewById(R.id.rbSpecijalan);
        btnSaveChanges = findViewById(R.id.btnUpdateTask);


        // postavljanje trenutne vrednosti taska i task instance
        int taskInstanceId = getIntent().getIntExtra("task_instance_id", -1);
        if (taskInstanceId != -1) {
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                TaskInstanceWithTask taskAndInstance = db.taskInstanceRepository().getTaskInstanceWithTaskById(taskInstanceId); // metoda u DAO

                runOnUiThread(() -> {
                    if (taskAndInstance != null) {
                        etName.setText(taskAndInstance.task.getName());
                        etDescription.setText(taskAndInstance.task.getDescription());
                        LocalDateTime startTime = taskAndInstance.taskInstance.getStartExecutionTime();
                        LocalDateTime endTime = taskAndInstance.taskInstance.getEndExecutionTime();

                        timePickerStart.setHour(startTime.getHour());
                        timePickerStart.setMinute(startTime.getMinute());

                        timePickerEnd.setHour(endTime.getHour());
                        timePickerEnd.setMinute(endTime.getMinute());

                        switch (taskAndInstance.taskInstance.getDifficultyInstance()) {
                            case VERY_EASY: rbVeomaLak.setChecked(true); break;
                            case EASY: rbLak.setChecked(true); break;
                            case HARD: rbTezak.setChecked(true); break;
                            case EXTREME: rbEkstremnoTezak.setChecked(true); break;
                        }

                        switch (taskAndInstance.taskInstance.getImportanceInstance()) {
                            case NORMAL: rbNormalan.setChecked(true); break;
                            case IMPORTANT: rbVazan.setChecked(true); break;
                            case VERY_IMPORTANT: rbEkstremnoVazan.setChecked(true); break;
                            case SPECIAL: rbSpecijalan.setChecked(true); break;
                        }
                    }
                });
            });
        }

        //uzimanje novih vrednosti i updatovanje ostalih
        btnSaveChanges.setOnClickListener(v -> {
            String newName = etName.getText().toString();
            String newDescription = etDescription.getText().toString();
            int startHour = timePickerStart.getHour();
            int startMinute = timePickerStart.getMinute();
            int endHour = timePickerEnd.getHour();
            int endMinute = timePickerEnd.getMinute();

            // Difficulty
            TaskInstance.DifficultyEnum newDifficulty = TaskInstance.DifficultyEnum.VERY_EASY;
            int selectedDiffId = rgDifficulty.getCheckedRadioButtonId();
            if (selectedDiffId == R.id.rbLak) newDifficulty = TaskInstance.DifficultyEnum.EASY;
            else if (selectedDiffId == R.id.rbTezak) newDifficulty = TaskInstance.DifficultyEnum.HARD;
            else if (selectedDiffId == R.id.rbEkstremnoTezak) newDifficulty = TaskInstance.DifficultyEnum.EXTREME;

            // Bitnost
            TaskInstance.ImportanceEnum newImportance = TaskInstance.ImportanceEnum.NORMAL;
            int selectedImpId = rgImportance.getCheckedRadioButtonId();
            if (selectedImpId == R.id.rbVazan) newImportance = TaskInstance.ImportanceEnum.IMPORTANT;
            else if (selectedImpId == R.id.rbEkstremnoVazan) newImportance = TaskInstance.ImportanceEnum.VERY_IMPORTANT;
            else if (selectedImpId == R.id.rbSpecijalan) newImportance = TaskInstance.ImportanceEnum.SPECIAL;

            final TaskInstance.DifficultyEnum finalDifficulty = newDifficulty;
            final TaskInstance.ImportanceEnum finalImportance = newImportance;
            // Ažuriranje task-a u bazi u pozadinskoj niti, to sam izvrsio da ne bi islo na glavnoj niti, da se ne bi preopterecivala
            // i da ne bi bila spora aplikacija, pogotovo ako imamo vise ovih podataka
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(this);

                TaskInstanceWithTask original = db.taskInstanceRepository().getTaskInstanceWithTaskById(taskInstanceId);
                if (original != null) {

                    // Uvek update-ujemo osnovne podatke Task-a
                    original.task.setName(newName);
                    original.task.setDescription(newDescription);
                    db.taskRepository().update(original.task);

                    LocalDateTime now = LocalDateTime.now();

                    // Ako je task ONE_TIME
                    if (original.task.getFrequency() == Task.FrequencyEnum.ONE_TIME) {
                        if (!original.taskInstance.getStartExecutionTime().isBefore(now)) {
                            // Menjamo samo tu jednu instancu
                            original.taskInstance.setDifficultyInstance(finalDifficulty);
                            original.taskInstance.setImportanceInstance(finalImportance);

                            LocalDateTime startTime = original.taskInstance.getStartExecutionTime()
                                    .withHour(startHour).withMinute(startMinute);
                            LocalDateTime endTime = original.taskInstance.getEndExecutionTime()
                                    .withHour(endHour).withMinute(endMinute);

                            original.taskInstance.setStartExecutionTime(startTime);
                            original.taskInstance.setEndExecutionTime(endTime);

                            db.taskInstanceRepository().update(original.taskInstance);

                            runOnUiThread(() -> {
                                Toast.makeText(this, "Jednokratni task uspešno ažuriran!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Task je prošao, ne može da se ažurira!", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    // Ako je task REPEATING
                    else if (original.task.getFrequency() == Task.FrequencyEnum.REPEATING) {
                        // Uzimamo sve buduće instance
                        List<TaskInstanceWithTask> futureInstances =
                                db.taskInstanceRepository().getFutureTaskInstancesWithTaskById(original.task.getId(), now);

                        if(!futureInstances.isEmpty())
                        {
                            for (TaskInstanceWithTask ti : futureInstances) {
                                Log.d("FUTURE_INSTANCE_BEFORE",
                                        "TaskInstanceId=" + ti.taskInstance.getId() +
                                                ", TaskId=" + ti.taskInstance.getTaskId() +
                                                ", Name=" + ti.task.getName() +
                                                ", Start=" + ti.taskInstance.getStartExecutionTime() +
                                                ", End=" + ti.taskInstance.getEndExecutionTime() +
                                                ", Difficulty=" + ti.taskInstance.getDifficultyInstance() +
                                                ", Importance=" + ti.taskInstance.getImportanceInstance());
                                ti.taskInstance.setDifficultyInstance(finalDifficulty);
                                ti.taskInstance.setImportanceInstance(finalImportance);

                                LocalDateTime startTime = ti.taskInstance.getStartExecutionTime()
                                        .withHour(startHour).withMinute(startMinute);
                                LocalDateTime endTime = ti.taskInstance.getEndExecutionTime()
                                        .withHour(endHour).withMinute(endMinute);

                                ti.taskInstance.setStartExecutionTime(startTime);
                                ti.taskInstance.setEndExecutionTime(endTime);
                                db.taskInstanceRepository().update(ti.taskInstance);
                                Log.d("FUTURE_INSTANCE_AFTER",
                                        "TaskInstanceId=" + ti.taskInstance.getId() +
                                                ", TaskId=" + ti.taskInstance.getTaskId() +
                                                ", Name=" + ti.task.getName() +
                                                ", Start=" + ti.taskInstance.getStartExecutionTime() +
                                                ", End=" + ti.taskInstance.getEndExecutionTime() +
                                                ", Difficulty=" + ti.taskInstance.getDifficultyInstance() +
                                                ", Importance=" + ti.taskInstance.getImportanceInstance());
                            }

                            runOnUiThread(() -> {
                                Toast.makeText(this, "Repeating task i sve buduće instance uspešno ažurirani!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }
                        else
                        {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Nema taskova za azuriranje!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }

                    }
                }
            });

        });
    }

}
