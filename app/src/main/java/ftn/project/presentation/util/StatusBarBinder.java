package ftn.project.presentation.util;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;

import ftn.project.presentation.vm.StatusBarViewModel;

public class StatusBarBinder {
    private StatusBarBinder(){}

    public static void bind(AppCompatActivity act, TextView tvXP, TextView tvPP, TextView tvCoins){
        StatusBarViewModel vm = new ViewModelProvider(act).get(StatusBarViewModel.class);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        vm.setUid(uid);

        vm.stats.observe(act, s -> {
            if (s == null) return;
            tvCoins.setText(java.text.NumberFormat.getInstance().format(s.coins));
            tvPP.setText(java.text.NumberFormat.getInstance().format(s.powerPoints));
            tvXP.setText(java.text.NumberFormat.getInstance().format(s.experiencePoints));
        });
    }
}
