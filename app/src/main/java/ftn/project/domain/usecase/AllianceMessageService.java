package ftn.project.domain.usecase;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.AllianceMessage;

public class AllianceMessageService {
    private final FirebaseFirestore fs;
    private AppDatabase db;

    public AllianceMessageService(Context ctx) {
        this.fs = FirebaseFirestore.getInstance();
        db = AppDatabase.getInstance(ctx);
    }
    public Task<Integer> createMessage(AllianceMessage message, Runnable onDone) {
        com.google.firebase.firestore.DocumentReference counterRef =
                fs.collection("counters").document("messages");
        DocumentReference messageRef = fs.collection("messages").document();

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
            data.put("messageId", next);
            data.put("creatorUserId", message.getCreatorUserId());
            data.put("creatorUsername", message.getCreatorUsername());
            data.put("content", message.getContent());
            data.put("sentAt", message.getSentAt());
            data.put("allianceId", message.getAllianceId());

            tr.set(messageRef, data);
            return (int) next;  // rezultat transakcije
        }).addOnSuccessListener(messageId -> {
            if (onDone != null) new Handler(Looper.getMainLooper()).post(onDone);
            Log.d("FS_SYNC", "Created message with intId: " + messageId);
        }).addOnFailureListener(e -> {
            Log.e("FS_SYNC", "Failed to create alliance", e);
        });
    }
}
