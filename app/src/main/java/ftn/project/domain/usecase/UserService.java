package ftn.project.domain.usecase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.User;

public class UserService {
    private AppDatabase db;

    public UserService(Context ctx) {
        db = AppDatabase.getInstance(ctx);
    }

    public void removeUsersFromAlliance(int allianceId){
        List<User> users = db.userRepository().getAll();

        for(User u : users){
            Integer ua = u.getAllianceId();    // može biti null
            if (ua != null && ua.intValue() == allianceId) {
                db.userRepository().removeFromAlliance(u.getUserId());
            }
        }
    }

    public Task<Void> setUserAllianceIdByIntId(int userIntId, @Nullable Integer allianceId) {
        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        return fs.collection("users")
                .whereEqualTo("intId", userIntId)
                .limit(1)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();

                    var snap = task.getResult();
                    if (snap == null || snap.isEmpty()) {
                        throw new IllegalStateException("User not found for intId=" + userIntId);
                    }

                    DocumentReference doc = snap.getDocuments().get(0).getReference();

                    return doc.update("allianceId", allianceId);
                })
                .addOnSuccessListener(v ->
                        Log.i("FS_SYNC", "Set allianceId=" + allianceId + " for user intId=" + userIntId))
                .addOnFailureListener(e ->
                        Log.e("FS_SYNC", "Failed setting allianceId for intId=" + userIntId, e));
    }

}
