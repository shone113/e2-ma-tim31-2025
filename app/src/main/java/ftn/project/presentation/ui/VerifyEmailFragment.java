package ftn.project.presentation.ui;

import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ftn.project.R;
public class VerifyEmailFragment extends Fragment {
    private final Handler handler = new Handler();
    private Runnable pollTask;
    private static final long POLL_INTERVAL_MS = 4000;

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        startPolling();
    }

    private void startPolling() {
        pollTask = new Runnable() {
            @Override public void run() {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Niste prijavljeni.", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                    return;
                }
                user.reload().addOnCompleteListener(task -> {
                    if (!isAdded()) return; // fragment više nije u UI-ju

                    if (task.isSuccessful() && user.isEmailVerified()) {
                        // STOP sve callback-ove pre navigacije
                        handler.removeCallbacksAndMessages(null);
                        // Navigacija (proveri da li je activity još tu)
                        if (getActivity() != null) {
                            startActivity(new Intent(requireActivity(), MainActivity.class));
                            requireActivity().finish();
                        }
                    } else {
                        // zakaži sledeću proveru samo ako je fragment živ
                        if (isAdded()) handler.postDelayed(pollTask, POLL_INTERVAL_MS);
                    }
                });
            }
        };
        handler.post(pollTask);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
