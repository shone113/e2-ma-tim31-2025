package ftn.project.presentation.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.usecase.LoggedUserService;
import ftn.project.domain.usecase.StatsService;

public class TaskStatusFragment extends Fragment {

    private PieChart chart;
    private LoggedUserService loggedUserService;

    public static TaskStatusFragment newInstance() { return new TaskStatusFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chart = view.findViewById(R.id.tasksDonutChart);

        AppDatabase db = AppDatabase.getInstance(requireContext());
        loggedUserService = new LoggedUserService(requireContext());

        int userId = loggedUserService.getCurrentUser().getUserId();
        StatsService stats = new StatsService(db, userId);

        String[] keys = {"Created", "Done", "Unfinished", "Canceled"};
        int[] palette = {
                Color.parseColor("#0D47A1"), // Created - dark blue
                Color.parseColor("#388E3C"), // Done - green
                Color.parseColor("#42A5F5"), // Unfinished - light blue
                Color.parseColor("#D32F2F")  // Canceled - red
        };
        List<Integer> colors = new ArrayList<>();

        Map<String, Integer> counts = stats.getTaskStatusCounts(userId);

        List<PieEntry> entries = new ArrayList<>();
        int total = 0, i = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            int v = e.getValue() != null ? e.getValue() : 0;
            total += v;
            if (v > 0) {
                entries.add(new PieEntry(v, e.getKey()));
                colors.add(palette[i]);
            }
            i++;
        }

        PieDataSet set = new PieDataSet(entries, "");
        set.setSliceSpace(2f);
        set.setValueTextSize(12f);
        set.setValueTextColor(Color.WHITE);
//        set.setColors(ColorTemplate.MATERIAL_COLORS);
        set.setColors(colors);

        PieData data = new PieData(set);
        data.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                return String.valueOf((int) value); // prikazuj cele brojeve
            }
        });

        chart.setData(data);
        chart.setUsePercentValues(false);
        chart.setDrawEntryLabels(true);
        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTextSize(12f);

        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(55f);
        chart.setTransparentCircleRadius(60f);
        chart.setHoleColor(Color.TRANSPARENT);

        chart.setCenterText("Total\n" + total);
        chart.setCenterTextColor(Color.WHITE);
        chart.setCenterTextSize(14f);

        chart.getDescription().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setTextColor(Color.WHITE);

        chart.animateY(800);
        chart.invalidate();

        if (entries.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No task data");
            chart.setNoDataTextColor(Color.WHITE);
        }

    }
}
