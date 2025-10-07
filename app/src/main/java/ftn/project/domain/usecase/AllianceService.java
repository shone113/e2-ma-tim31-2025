package ftn.project.domain.usecase;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ftn.project.data.db.AppDatabase;
import ftn.project.data.dto.UserFriendDTO;
import ftn.project.domain.entity.AllianceStatus;
import ftn.project.domain.entity.User;
import kotlin.collections.ArrayDeque;

public class AllianceService {
    private final FirebaseFirestore fs;
    private AppDatabase db;

    public AllianceService(Context ctx) {
        this.fs = FirebaseFirestore.getInstance();
        db = AppDatabase.getInstance(ctx);
    }
    public Task<Integer> createAlliance(String name, Integer leaderUserId, AllianceStatus allianceStatus,  Runnable onDone) {
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
            data.put("allianceStatus", allianceStatus);

            tr.set(allianceRef, data);
            return (int) next;  // rezultat transakcije
        }).addOnSuccessListener(allianceId -> {
            if (onDone != null) new Handler(Looper.getMainLooper()).post(onDone);
            Log.d("FS_SYNC", "Created alliance with intId: " + allianceId);
        }).addOnFailureListener(e -> {
            Log.e("FS_SYNC", "Failed to create alliance", e);
        });
    }

    public Task<Void> disbandAlliance(int allianceId) {
        return fs.collection("alliances")
                .whereEqualTo("allianceId", allianceId)
                .limit(1)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    var snap = task.getResult();
                    if (snap == null || snap.isEmpty()) {
                        throw new IllegalStateException("Alliance not found for allianceId=" + allianceId);
                    }

                    DocumentReference doc = snap.getDocuments().get(0).getReference();

                    Map<String, Object> patch = new HashMap<>();
                    patch.put("allianceStatus", "DISBANDED");
                    patch.put("disbandedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

                    // merge → ne briše postojeća polja (name, leaderUserId, members...)
                    return doc.set(patch, SetOptions.merge());
                })
                .addOnSuccessListener(v -> {
                    Log.i("FS_SYNC", "Alliance " + allianceId + " disbanded on Firestore");
                })
                .addOnFailureListener(e -> Log.e("FS_SYNC", "Disband failed for allianceId=" + allianceId, e));
    }

    public Task<Void> deleteAllianceInvitesByAllianceId(int allianceId) {
        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        return fs.collection("allianceInvites")
                .whereEqualTo("allianceId", allianceId)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();

                    QuerySnapshot snap = task.getResult();
                    if (snap == null || snap.isEmpty()) {
                        return com.google.android.gms.tasks.Tasks.forResult(null);
                    }

                    List<Task<Void>> deletes = new ArrayList<>(snap.size());
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        deletes.add(d.getReference().delete());
                    }
                    // sačekaj da se svi delete pozivi završe
                    return com.google.android.gms.tasks.Tasks.whenAll(deletes);
                });
    }


    public Task<Void> deleteAllianceInvitesByInviteeUserId(int allianceId, int inviteeUserId) {
        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        return fs.collection("allianceInvites")
                .whereEqualTo("allianceId", allianceId)
                .whereEqualTo("inviteeUserId", inviteeUserId)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();

                    QuerySnapshot snap = task.getResult();
                    if (snap == null || snap.isEmpty()) {
                        return com.google.android.gms.tasks.Tasks.forResult(null);
                    }

                    List<Task<Void>> deletes = new ArrayList<>(snap.size());
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        deletes.add(d.getReference().delete());
                    }
                    // sačekaj da se svi delete pozivi završe
                    return com.google.android.gms.tasks.Tasks.whenAll(deletes);
                });
    }

}
