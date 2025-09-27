package ftn.project.presentation.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.usecase.LoggedUserService;
import ftn.project.domain.usecase.StatsService;

public class TrendsFragment extends Fragment {

    private MaterialButtonToggleGroup toggle;
    private LineChart chart;
    private TextView hint;

    private LoggedUserService loggedUserService;
    private StatsService statsService;
    public static TrendsFragment newInstance() { return new TrendsFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toggle = view.findViewById(R.id.trendToggle);
        chart  = view.findViewById(R.id.trendLineChart);
        hint   = view.findViewById(R.id.trendHint);

        AppDatabase db = AppDatabase.getInstance(requireContext());
        loggedUserService = new LoggedUserService(requireContext());
        int userId = loggedUserService.getCurrentUser().getUserId();
        statsService = new StatsService(db, userId);

        setupChartAppearance();

        renderAvgDifficulty();

        // Reakcija na klik dugmadi
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnDifficulty) {
                renderAvgDifficulty();
            } else if (checkedId == R.id.btnXp) {
                renderXp7Days();
            }
        });

        // Podrazumevano
        toggle.check(R.id.btnDifficulty);
    }

    private void setupChartAppearance() {
        chart.setNoDataText("No data");
        chart.setNoDataTextColor(Color.WHITE);
        chart.getDescription().setEnabled(false);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setTextColor(Color.WHITE);
        x.setAxisLineColor(Color.WHITE);
        x.setDrawGridLines(false);

        YAxis y = chart.getAxisLeft();
        y.setTextColor(Color.WHITE);
        y.setAxisLineColor(Color.WHITE);
        y.setGridColor(Color.DKGRAY);
        y.setAxisMinimum(0f); // start from 0
        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setTextColor(Color.WHITE);
    }
    private void renderAvgDifficulty() {
        Map<String, Float> series = statsService.getAvgDifficultyPerDay(loggedUserService.getCurrentUser().getUserId());

        List<Map.Entry<String, Float>> pairs = new ArrayList<>(series.entrySet());

        List<String> labels = new ArrayList<>(pairs.size());
        List<Entry> entries = new ArrayList<>(pairs.size());
        for (int i = 0; i < pairs.size(); i++) {
            String label = pairs.get(i).getKey();
            Float v = pairs.get(i).getValue();
            if (v == null || Float.isNaN(v)) v = 0f;
            labels.add(label);
            entries.add(new Entry(i, v));
        }

        LineDataSet set = new LineDataSet(entries, "Avg difficulty (1–4)");
        set.setDrawValues(false);
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // 2) Zadaj Y=1..4 za ovaj graf
        YAxis y = chart.getAxisLeft();
        y.setAxisMinimum(1f);
        y.setAxisMaximum(4f);
        y.setGranularity(1f);

        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setLabelCount(labels.size(), true);

        chart.setData(new LineData(set));
        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private void renderXp7Days() {
        Map<String, Integer> xp = statsService.getXpLast7Days(loggedUserService.getCurrentUser().getUserId());

        List<Map.Entry<String, Integer>> pairs = new ArrayList<>(xp.entrySet());

        List<String> labels = new ArrayList<>(pairs.size());
        List<Entry> entries = new ArrayList<>(pairs.size());
        for (int i = 0; i < pairs.size(); i++) {
            String label = pairs.get(i).getKey();
            Integer v = pairs.get(i).getValue();
            float fv = (v == null ? 0f : v.floatValue());
            labels.add(label);
            entries.add(new Entry(i, fv));
        }

        LineDataSet set = new LineDataSet(entries, "XP (last 7 days)");
        set.setDrawValues(false);
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // 2) Vrati Y osu na auto-range za XP (ili bar min=0)
        YAxis y = chart.getAxisLeft();
        y.resetAxisMinimum();
        y.resetAxisMaximum();
        y.setAxisMinimum(0f); // tipično ima smisla za XP; ukloni ako želiš full auto
        chart.getAxisRight().setEnabled(false);

        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setLabelCount(labels.size(), true);

        chart.setData(new LineData(set));
        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();

        int total = 0;
        for (Map.Entry<String, Integer> e : pairs) total += (e.getValue() != null ? e.getValue() : 0);
        hint.setText("XP u poslednjih 7 dana: " + total);
    }
}
