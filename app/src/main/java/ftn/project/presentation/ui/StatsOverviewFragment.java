package ftn.project.presentation.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Map;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.usecase.LoggedUserService;
import ftn.project.domain.usecase.StatsService;

public class StatsOverviewFragment extends Fragment {

    private TextView tvStreak, tvStreakHint, tvBestStreak, tvBestStreakHint, tvMissionsCount;
    private ProgressBar pbMissions;
    private StatsService statsService;
    private LoggedUserService logged;

    private LoggedUserService loggedUserService;
    public static StatsOverviewFragment newInstance() { return new StatsOverviewFragment(); }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        return inf.inflate(R.layout.fragment_stats_overview, c, false);
    }


    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        tvStreak = v.findViewById(R.id.tvStreak);
        tvStreakHint = v.findViewById(R.id.tvStreakHint);
        tvBestStreak = v.findViewById(R.id.tvBestStreak);
        tvBestStreakHint = v.findViewById(R.id.tvBestStreakHint);
        tvMissionsCount = v.findViewById(R.id.tvMissionsCount);
        pbMissions = v.findViewById(R.id.pbMissions);

        AppDatabase db = AppDatabase.getInstance(requireContext());
        logged = new LoggedUserService(requireContext());
        statsService = new StatsService(db, logged.getCurrentUser().getUserId());

        bindData();
    }

    private void bindData() {
        int bestStreak    = statsService.getBestStreakDays();
        Map<String, Integer> missionStats = statsService.getSpecialMissionStats();
        int finished   = missionStats.getOrDefault("Finished", 0);
        int unfinished = missionStats.getOrDefault("Unfinished", 0);

        int started = finished + unfinished;

        tvMissionsCount.setText(finished + " / " + started);

        int pct = (started == 0) ? 0 : Math.round(100f * finished / (float) started);
        pct = Math.max(0, Math.min(100, pct)); // clamp na [0,100]

        pbMissions.setProgress(pct, true);
    }
}
