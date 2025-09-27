package ftn.project.presentation.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.data.dto.CategoryBarData;
import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.TaskInstance;
import ftn.project.domain.entity.TaskInstanceWithTask;
import ftn.project.domain.usecase.LoggedUserService;
import ftn.project.domain.usecase.StatsService;

public class CategoriesBarFragment extends Fragment {

    private AppDatabase db;
    private LoggedUserService loggedUserService;
    private StatsService statsService;
    public static CategoriesBarFragment newInstance() {
        CategoriesBarFragment f = new CategoriesBarFragment();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // npr. R.layout.fragment_categories_bar sa tvojim grafom
        return inflater.inflate(R.layout.fragment_categories_bar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppDatabase db = AppDatabase.getInstance(requireContext());
        loggedUserService = new LoggedUserService(requireContext());

        int userId = loggedUserService.getCurrentUser().getUserId();
        statsService = new StatsService(db, userId);

        BarChart chart = view.findViewById(R.id.categoriesBarChart);
        CategoryBarData data = statsService.getFinishedTasksByCategory(userId);

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < data.getValues().size(); i++) {
            entries.add(new BarEntry(i, data.getValues().get(i)));
        }

        BarDataSet set = new BarDataSet(entries, "Završeni zadaci");
        set.setValueTextSize(14f);
        set.setColors(data.getColors()); // << boje po kategoriji

        BarData barData = new BarData(set);
        chart.setData(barData);

        YAxis y = chart.getAxisLeft();
        y.setAxisMinimum(0f);            // startuj od 0
        y.setGranularity(1f);            // korak 1 (opciono)
        y.setGranularityEnabled(true);   // uključi granularnost
        y.setAxisLineColor(Color.WHITE);
        y.setSpaceTop(0f);               // manje praznog prostora gore (opciono)

        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.WHITE);        // ← tekst na X osi (nazivi)
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(data.getCategories()));

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.animateY(800);
        chart.invalidate();
    }

    // Po potrebi public metod za osvežavanje iz Activity-ja:
    public void refreshData() {
        // ponovo učitaj podatke i apdejtuj graf
    }
}
