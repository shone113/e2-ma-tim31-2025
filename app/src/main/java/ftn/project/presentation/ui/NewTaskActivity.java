package ftn.project.presentation.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.TaskInstance;

public class NewTaskActivity extends AppCompatActivity {

    // polja
    private EditText etNaziv, etOpis, etInterval;
    private RadioGroup rgUestalost, rgTezina, rgBitnost;
    private CalendarView calendarViewStart, calendarViewEnd;
    private TimePicker startTimeExecution, endTimeExecution;
    private Spinner spinnerFrequencyUnit;

    private LocalDate startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task);

        // poveži polja
        etNaziv = findViewById(R.id.etNaziv);
        etOpis = findViewById(R.id.etOpis);
        rgUestalost = findViewById(R.id.rgUestalost);
        rgTezina = findViewById(R.id.rgTezina);
        rgBitnost = findViewById(R.id.rgBitnost);
        calendarViewStart = findViewById(R.id.calendarViewStart);
        calendarViewEnd = findViewById(R.id.calendarViewEnd);
        spinnerFrequencyUnit = findViewById(R.id.spinnerFrequencyUnit);
        etInterval = findViewById(R.id.intervalText);
        startTimeExecution = findViewById(R.id.timePickerStart);
        endTimeExecution = findViewById(R.id.timePickerEnd);

        // pamti izabrane datume
        calendarViewStart.setOnDateChangeListener((view, year, month, dayOfMonth) ->
                startDate = LocalDate.of(year, month + 1, dayOfMonth)
        );
        calendarViewEnd.setOnDateChangeListener((view, year, month, dayOfMonth) ->
                endDate = LocalDate.of(year, month + 1, dayOfMonth)
        );

        // dugme Sačuvaj
        Button btnSacuvaj = findViewById(R.id.btnSacuvaj);
        btnSacuvaj.setOnClickListener(v -> saveTask());

        // logika za sakrivanje/prikazivanje delova
        LinearLayout startDateContainer = findViewById(R.id.startDateContainer);
        LinearLayout endDateContainer = findViewById(R.id.endDateContainer);
        LinearLayout timeStartLinearLayout = findViewById(R.id.timeContainer);
        LinearLayout timeEndLinearLayout = findViewById(R.id.timeContainer2);
        LinearLayout frequencyLayout = findViewById(R.id.frequencyUnitContainer);
        TextView tvPocetniDatum = findViewById(R.id.tvPocetniDatum);
        EditText intervalNumber = findViewById(R.id.intervalText);

        startDateContainer.setVisibility(View.GONE);
        endDateContainer.setVisibility(View.GONE);
        timeStartLinearLayout.setVisibility(View.GONE);
        timeEndLinearLayout.setVisibility(View.GONE);
        frequencyLayout.setVisibility(View.GONE);
        intervalNumber.setVisibility(View.GONE);

        rgUestalost.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbJednokratna) {
                startDateContainer.setVisibility(View.VISIBLE);
                endDateContainer.setVisibility(View.GONE);
                frequencyLayout.setVisibility(View.GONE);
                tvPocetniDatum.setText("Datum izvršavanja");
                timeStartLinearLayout.setVisibility(View.VISIBLE);
                timeEndLinearLayout.setVisibility(View.VISIBLE);
                intervalNumber.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbPonavljajuci) {
                startDateContainer.setVisibility(View.VISIBLE);
                endDateContainer.setVisibility(View.VISIBLE);
                timeStartLinearLayout.setVisibility(View.VISIBLE);
                timeEndLinearLayout.setVisibility(View.VISIBLE);
                frequencyLayout.setVisibility(View.VISIBLE);
                intervalNumber.setVisibility(View.VISIBLE);
            }
        });
    }

    private void saveTask() {
        String name = etNaziv.getText().toString();
        String description = etOpis.getText().toString();

        // Težina
        Task.DifficultyEnum difficulty = Task.DifficultyEnum.VERY_EASY;
        int selectedDiffId = rgTezina.getCheckedRadioButtonId();
        if (selectedDiffId == R.id.rbLak) difficulty = Task.DifficultyEnum.EASY;
        else if (selectedDiffId == R.id.rbTezak) difficulty = Task.DifficultyEnum.HARD;
        else if (selectedDiffId == R.id.rbEkstremnoTezak) difficulty = Task.DifficultyEnum.EXTREME;

        // Bitnost
        Task.ImportanceEnum importance = Task.ImportanceEnum.NORMAL;
        int selectedImpId = rgBitnost.getCheckedRadioButtonId();
        if (selectedImpId == R.id.rbVazan) importance = Task.ImportanceEnum.IMPORTANT;
        else if (selectedImpId == R.id.rbEkstremnoVazan) importance = Task.ImportanceEnum.VERY_IMPORTANT;
        else if (selectedImpId == R.id.rbSpecijalan) importance = Task.ImportanceEnum.SPECIAL;

        // Učestalost
        Task.FrequencyEnum frequency = Task.FrequencyEnum.ONE_TIME;
        Task.FrequencyUnitEnum frequencyUnit = null;
        Integer interval = null;
        if (rgUestalost.getCheckedRadioButtonId() == R.id.rbPonavljajuci) {
            frequency = Task.FrequencyEnum.REPEATING;
            String selectedUnit = spinnerFrequencyUnit.getSelectedItem().toString();
            frequencyUnit = selectedUnit.equals("Dani") ? Task.FrequencyUnitEnum.DAY : Task.FrequencyUnitEnum.WEEK;

            String intervalStr = etInterval.getText().toString();
            if (!intervalStr.isEmpty()) {
                try {
                    interval = Integer.parseInt(intervalStr);
                } catch (NumberFormatException e) {
                    interval = 1; // podrazumevana vrednost ako unos nije validan
                }
            } else {
                interval = 1; // podrazumevana vrednost ako ništa nije uneto
            }
        }

        int startHour = startTimeExecution.getHour();
        int startMinute = startTimeExecution.getMinute();
        LocalTime startTime = LocalTime.of(startHour, startMinute);

        int endHour = endTimeExecution.getHour();
        int endMinute = endTimeExecution.getMinute();
        LocalTime endTime = LocalTime.of(endHour, endMinute);

        LocalDateTime startDateTime = startDate != null ? startDate.atTime(startTime) : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(endTime) : null;
        // Napravi task
        Task task = new Task(
                0,
                1, // userId test
                1, // categoryId = 1
                difficulty,
                importance,
                frequency,
                interval != null ? interval : 0,
                frequencyUnit,
                startDateTime,
                endDateTime,
                name,
                description
        );

        // Snimi u bazu
        AppDatabase db = AppDatabase.getInstance(this);
        long taskId = db.taskRepository().insert(task); // insert vraća id

        // Kreiraj TaskInstance-e
        if (frequency == Task.FrequencyEnum.REPEATING && startDateTime != null && endDateTime != null) {
            LocalDateTime current = startDateTime;
            while (!current.isAfter(endDateTime)) {
                TaskInstance instance = new TaskInstance(
                        0,
                        (int)taskId,
                        current,
                        current.withHour(endTime.getHour()).withMinute(endTime.getMinute()),
                        TaskInstance.TaskStatusEnum.ACTIVE
                );
                db.taskInstanceRepository().insert(instance);

                // povećaj datum po intervalu
                if (frequencyUnit == Task.FrequencyUnitEnum.DAY) current = current.plusDays(interval);
                else current = current.plusWeeks(interval);
            }
        } else if (startDateTime != null) {
            TaskInstance instance = new TaskInstance(
                    0,
                    (int)taskId,
                    startDateTime,
                    startDateTime.withHour(endTime.getHour()).withMinute(endTime.getMinute()),
                    TaskInstance.TaskStatusEnum.ACTIVE
            );
            db.taskInstanceRepository().insert(instance);
        }

        List<Task> sviTaskovi = db.taskRepository().getAllTasks();
        for (Task t : sviTaskovi) {
            Log.d("ROOM_CHECK", "Task: id=" + t.getId() +
                    ", name=" + t.getName() +
                    ", description=" + t.getDescription() +
                    ", userId=" + t.getUserId() +
                    ", categoryId=" + t.getCategoryId() +
                    ", difficulty=" + t.getDifficulty() +
                    ", importance=" + t.getImportance() +
                    ", frequency=" + t.getFrequency() +
                    ", interval=" + t.getInterval() +
                    ", frequencyUnit=" + t.getFrequencyUnit() +
                    ", startDate=" + t.getStartDate() +
                    ", endDate=" + t.getEndDate() +
                    ", valueXP=" + t.getValueXP());
        }

        List<TaskInstance> instance = db.taskInstanceRepository().getAllTasksInstances();
        for (TaskInstance t : instance) {
            Log.d("Instance", "Task Instanca: id=" + t.getId() +
                    ", task=" + t.getTaskId() +
                    ", start=" + t.getStartExecutionTime() +
                    ", end=" + t.getEndExecutionTime() +
                    ", status=" + t.getStatus());
        }



        Toast.makeText(this, "Zadatak sačuvan (Room)!", Toast.LENGTH_SHORT).show();
    }
}
