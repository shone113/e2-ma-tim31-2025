package ftn.project.presentation.ui;
/*Ukratko: UI elementi u Androidu mogu da se menjaju samo sa glavne (UI) niti.

Kad ti koristiš Executors.newSingleThreadExecutor(), kod unutar njega se izvršava u pozadinskoj niti (background thread). Ako pokušaš direktno da pozoveš setText, setChecked ili slično, Android baca grešku CalledFromWrongThreadException.

runOnUiThread prebacuje taj kod na glavnu nit, pa je bezbedno menjati TextView, Button, TimePicker i sve ostale UI komponente.

Jednostavno: background thread radi posao (npr. čitanje iz baze), runOnUiThread ažurira ekran.*/
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

                        switch (taskAndInstance.task.getDifficulty()) {
                            case VERY_EASY: rbVeomaLak.setChecked(true); break;
                            case EASY: rbLak.setChecked(true); break;
                            case HARD: rbTezak.setChecked(true); break;
                            case EXTREME: rbEkstremnoTezak.setChecked(true); break;
                        }

                        switch (taskAndInstance.task.getImportance()) {
                            case NORMAL: rbNormalan.setChecked(true); break;
                            case IMPORTANT: rbVazan.setChecked(true); break;
                            case VERY_IMPORTANT: rbEkstremnoVazan.setChecked(true); break;
                            case SPECIAL: rbSpecijalan.setChecked(true); break;
                        }
                    }
                });
            });
        }
        btnSaveChanges.setOnClickListener(v -> {
            // Uzimanje novih vrednosti sa ekrana
            String newName = etName.getText().toString();
            String newDescription = etDescription.getText().toString();
            int startHour = timePickerStart.getHour();
            int startMinute = timePickerStart.getMinute();
            int endHour = timePickerEnd.getHour();
            int endMinute = timePickerEnd.getMinute();

            // Difficulty
            Task.DifficultyEnum newDifficulty = Task.DifficultyEnum.VERY_EASY;
            int selectedDiffId = rgDifficulty.getCheckedRadioButtonId();
            if (selectedDiffId == R.id.rbLak) newDifficulty = Task.DifficultyEnum.EASY;
            else if (selectedDiffId == R.id.rbTezak) newDifficulty = Task.DifficultyEnum.HARD;
            else if (selectedDiffId == R.id.rbEkstremnoTezak) newDifficulty = Task.DifficultyEnum.EXTREME;

            // Bitnost
            Task.ImportanceEnum newImportance = Task.ImportanceEnum.NORMAL;
            int selectedImpId = rgImportance.getCheckedRadioButtonId();
            if (selectedImpId == R.id.rbVazan) newImportance = Task.ImportanceEnum.IMPORTANT;
            else if (selectedImpId == R.id.rbEkstremnoVazan) newImportance = Task.ImportanceEnum.VERY_IMPORTANT;
            else if (selectedImpId == R.id.rbSpecijalan) newImportance = Task.ImportanceEnum.SPECIAL;

            final Task.DifficultyEnum finalDifficulty = newDifficulty;
            final Task.ImportanceEnum finalImportance = newImportance;
            // Ažuriranje task-a u bazi u pozadinskoj niti, to sam izvrsio da ne bi islo na glavnoj niti, da se ne bi preopterecivala
            // i da ne bi bila spora aplikacija, pogotovo ako imamo vise ovih podataka
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(this);

                TaskInstanceWithTask original = db.taskInstanceRepository().getTaskInstanceWithTaskById(taskInstanceId);
                if (original != null) {
                    original.task.setName(newName);
                    original.task.setDescription(newDescription);
                    original.task.setDifficulty(finalDifficulty);
                    original.task.setImportance(finalImportance);

                    LocalDateTime startTime = original.taskInstance.getStartExecutionTime()
                            .withHour(startHour).withMinute(startMinute);
                    LocalDateTime endTime = original.taskInstance.getEndExecutionTime()
                            .withHour(endHour).withMinute(endMinute);

                    original.taskInstance.setStartExecutionTime(startTime);
                    original.taskInstance.setEndExecutionTime(endTime);

                    db.taskRepository().update(original.task);
                    LocalDateTime now = LocalDateTime.now();
                    if(!original.taskInstance.getStartExecutionTime().isBefore(now))
                        db.taskInstanceRepository().update(original.taskInstance);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Task uspešno ažuriran!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

            });
        });
    }

}
