package ftn.project.presentation.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.Executors;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Title;
import ftn.project.domain.entity.User;

public class ProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "ftn.project.EXTRA_USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int userId = getIntent().getIntExtra(EXTRA_USER_ID, -1);
        if (userId == -1) { finish(); return; }

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        User user = db.userRepository().getById(userId);

        TextView tvUsername = findViewById(R.id.tvUsername);
        TextView tvTitle = findViewById(R.id.tvTitle);
        ImageView imgTitle = findViewById(R.id.imgTitle);

        Title t = Title.fromLevel(user.getLevel());
        int nameId = getResources().getIdentifier(t.nameKey, "string", getPackageName());
        int iconId = getResources().getIdentifier(t.iconKey, "drawable", getPackageName());
        tvTitle.setText(nameId);
        imgTitle.setImageResource(iconId);

        if(userId != -1){
            User u = AppDatabase.getInstance(this).userRepository().getById(userId);
            if(u != null){
                tvUsername.setText(u.getUsername());
            }
        }
    }
}