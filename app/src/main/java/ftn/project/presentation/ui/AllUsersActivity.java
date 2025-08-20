package ftn.project.presentation.ui;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.User;
import ftn.project.presentation.adapter.UserAdapter;

public class AllUsersActivity extends AppCompatActivity {

    private ArrayList<User> users;
    private UserAdapter adapter;

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

        AppDatabase db = AppDatabase.getInstance(this);
        User user1 = new User();
        User user2 = new User();
        users = new ArrayList<>();

        ListView lvUsers = findViewById(R.id.lvUsers);

        users = new ArrayList<>(db.userRepository().getAll());

        adapter = new UserAdapter(this, users, user -> {
            Toast.makeText(this,
                    "Dodao prijatelja:",
                    Toast.LENGTH_SHORT).show();
        });

        lvUsers.setAdapter(adapter);
    }
}