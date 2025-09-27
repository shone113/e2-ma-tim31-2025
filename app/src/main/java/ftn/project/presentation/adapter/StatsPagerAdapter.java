package ftn.project.presentation.adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ftn.project.presentation.ui.CategoriesBarFragment;
import ftn.project.presentation.ui.StatsOverviewFragment;
import ftn.project.presentation.ui.TaskStatusFragment;
import ftn.project.presentation.ui.TrendsFragment;

public class StatsPagerAdapter extends FragmentStateAdapter {

    public StatsPagerAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return StatsOverviewFragment.newInstance();
            case 1: return TaskStatusFragment.newInstance();
            case 2: return CategoriesBarFragment.newInstance();
            case 3: return TrendsFragment.newInstance();
            default: return CategoriesBarFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
