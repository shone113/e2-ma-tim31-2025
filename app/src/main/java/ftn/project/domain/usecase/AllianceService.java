package ftn.project.domain.usecase;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AllianceService {
    private final FirebaseFirestore fs;

    public AllianceService() {
        this.fs = FirebaseFirestore.getInstance();
    }
    public Task<Integer> createAlliance(String name, Integer leaderUserId, Runnable onDone) {
        com.google.firebase.firestore.DocumentReference counterRef =
                fs.collection("counters").document("alliances");
        DocumentReference allianceRef = fs.collection("alliances").document();

        return fs.runTransaction(tr -> {   // ✅ dodali smo return
            DocumentSnapshot c = tr.get(counterRef);
            long next;
            Long cur = (c.exists() ? c.getLong("nextId") : null);

            if (cur == null) {
                next = 100L;
                Map<String, Object> init = new HashMap<>();
                init.put("nextId", next + 1L);
                tr.set(counterRef, init);
            } else {
                next = cur;
                tr.update(counterRef, "nextId", cur + 1L);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("allianceId", next);
            data.put("name", name);
            data.put("leaderUserId", leaderUserId);
            data.put("createdAt", com.google.firebase.Timestamp.now());

            tr.set(allianceRef, data);
            return (int) next;  // rezultat transakcije
        }).addOnSuccessListener(allianceId -> {
            if (onDone != null) new Handler(Looper.getMainLooper()).post(onDone);
            Log.d("FS_SYNC", "Created alliance with intId: " + allianceId);
        }).addOnFailureListener(e -> {
            Log.e("FS_SYNC", "Failed to create alliance", e);
        });
    }
}
