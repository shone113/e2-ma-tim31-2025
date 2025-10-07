package ftn.project.domain.usecase;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.User;

public class LoggedUserService {
    private final AppDatabase db;
    private final Context context;

    public LoggedUserService(Context context) {
        this.context = context;
        this.db = AppDatabase.getInstance(context);
    }

    public User getCurrentUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String firebaseUid = firebaseUser.getUid();
        return db.userRepository().getByFirebaseUid(firebaseUid);
    }
}
