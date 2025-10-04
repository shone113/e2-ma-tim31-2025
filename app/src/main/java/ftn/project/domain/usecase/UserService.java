package ftn.project.domain.usecase;

import android.content.Context;

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
}
