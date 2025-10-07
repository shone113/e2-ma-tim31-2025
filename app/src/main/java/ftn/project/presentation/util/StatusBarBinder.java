package ftn.project.presentation.util;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;

import ftn.project.R;
import ftn.project.presentation.vm.StatusBarViewModel;

public class StatusBarBinder {
    private StatusBarBinder(){}

    public static void bind(AppCompatActivity act, ImageView ivTitle, TextView tvXP, TextView tvPP, TextView tvCoins){
        StatusBarViewModel vm = new ViewModelProvider(act).get(StatusBarViewModel.class);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        vm.setUid(uid);

        vm.stats.observe(act, s -> {
            if (s == null) return;
            ivTitle.setImageResource(mapTitleDrawableByLevel(s.level));
            tvCoins.setText(java.text.NumberFormat.getInstance().format(s.coins));
            tvPP.setText(java.text.NumberFormat.getInstance().format(s.powerPoints));
            tvXP.setText(java.text.NumberFormat.getInstance().format(s.experiencePoints));
        });
    }

    private static @DrawableRes int mapTitleDrawableByLevel(int level) {
        if (level == 0) return R.drawable.title_1;
        if (level == 1) return R.drawable.title_2;
        if (level == 2) return R.drawable.title_3;
        if (level == 3) return R.drawable.title_4;
        if (level == 4) return R.drawable.title_5;
        return R.drawable.title_1;
    }
}
