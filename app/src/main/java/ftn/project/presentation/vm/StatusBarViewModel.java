package ftn.project.presentation.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import ftn.project.data.db.AppDatabase;
import ftn.project.data.dto.UserStatsDTO;

public class StatusBarViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final MutableLiveData<String> uid = new MutableLiveData<>();
    public final LiveData<UserStatsDTO> stats;

    public StatusBarViewModel(@NonNull Application app) {
        super(app);
        db = AppDatabase.getInstance(app);
        stats = Transformations.switchMap(uid, u ->
                (u == null || u.isEmpty()) ? new MutableLiveData<>(null)
                        : db.userRepository().observeStats(u));
    }
    public void setUid(String u){ uid.setValue(u); }
}
