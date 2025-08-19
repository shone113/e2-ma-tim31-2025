package ftn.project.presentation.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ftn.project.R;

public class NewTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // poveži layout sa aktivnošću
        setContentView(R.layout.new_task);

        // poveži elemente iz XML-a
        RadioGroup rgUestalost = findViewById(R.id.rgUestalost);
        LinearLayout startDateContainer = findViewById(R.id.startDateContainer);
        LinearLayout endDateContainer = findViewById(R.id.endDateContainer);
        LinearLayout timeLinearLayout = findViewById(R.id.timeContainer);
        LinearLayout frequencyLayout = findViewById(R.id.frequencyUnitContainer);
        TextView tvPocetniDatum = findViewById(R.id.tvPocetniDatum);
        startDateContainer.setVisibility(View.GONE);
        endDateContainer.setVisibility(View.GONE);
        timeLinearLayout.setVisibility(View.GONE);
        frequencyLayout.setVisibility(View.GONE);

        // logika za prikazivanje/sakrivanje kalendara
        rgUestalost.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbJednokratna) {
                // Prikaz samo jednog kalendara
                startDateContainer.setVisibility(View.VISIBLE);
                endDateContainer.setVisibility(View.GONE);
                frequencyLayout.setVisibility(View.GONE);
                tvPocetniDatum.setText("Datum izvrsavanja");
                timeLinearLayout.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbPonavljajuci) {
                // Prikaz oba kalendara
                startDateContainer.setVisibility(View.VISIBLE);
                endDateContainer.setVisibility(View.VISIBLE);
                timeLinearLayout.setVisibility(View.VISIBLE);
                frequencyLayout.setVisibility(View.VISIBLE);
            }

        });
    }
}
