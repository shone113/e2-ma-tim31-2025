package ftn.project.presentation.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.data.dto.UserFriendDTO;
import ftn.project.data.repository.FriendshipRepository;
import ftn.project.domain.entity.Friendship;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.FriendshipService;
import ftn.project.presentation.adapter.UserAdapter;

public class AllUsersActivity extends AppCompatActivity {

    private UserAdapter adapter;
    private ArrayList<UserFriendDTO> friendDTOs;
    private FriendshipRepository friendshipRepository;
    private FriendshipService friendshipService;
    private AppDatabase db;
    private User loggedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_users);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = AppDatabase.getInstance(getApplicationContext());
        friendshipService = new FriendshipService();

        ListView lvUsers = findViewById(R.id.lvUsers);
        EditText etSearch = findViewById(R.id.etSearch);
        Button btnSearch = findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(v -> performSearch());

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        loggedUser = db.userRepository().getByFirebaseUid(firebaseUser.getUid());
        ArrayList<Friendship> friendships = new ArrayList<>(db.friendshipRepository().getAllForUserId(loggedUser.getUserId()));
        List<User> users = db.userRepository().getAll();
        friendDTOs = friendshipService.getFriendsForUser(friendships, users, loggedUser.getUserId());

        adapter = new UserAdapter(this, friendDTOs, userFriendDTO -> {
            Friendship friendship = new Friendship();
            friendship.setFirstUserId(loggedUser.getUserId());
            friendship.setSecondUserId(userFriendDTO.userId);
            db.friendshipRepository().insert(friendship);
        });

        lvUsers.setAdapter(adapter);
    }

    private void performSearch() {
        String q = ((EditText) findViewById(R.id.etSearch))
                .getText().toString().trim();
        if (q.length() < 2) {
            Toast.makeText(this, "Unesi bar 2 slova", Toast.LENGTH_SHORT).show();
            return;
        }
        List<UserFriendDTO> users = db.userRepository().searchNonFriendUsersWithFlag(q, loggedUser.getUserId());
        users.addAll(friendDTOs);

        adapter.replaceAll(users);
        users.clear();
//        adapter.clear();
//        adapter.addAll(users);
//        adapter.notifyDataSetChanged();
    }
}