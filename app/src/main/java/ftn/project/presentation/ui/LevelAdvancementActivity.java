package ftn.project.presentation.ui;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.data.dto.LevelDTO;
import ftn.project.domain.entity.Level;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.LevelAdvancementService;
import ftn.project.presentation.adapter.LevelAdvancementAdapter;
import ftn.project.presentation.adapter.ShopAdapter;
import ftn.project.presentation.util.StatusBarBinder;

public class LevelAdvancementActivity extends AppCompatActivity {

    private LevelAdvancementAdapter adapter;
    private LevelAdvancementService levelAdvancementService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_level_advancement);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //status bar
        TextView tvXP = findViewById(R.id.tvXP);
        TextView tvPP = findViewById(R.id.tvPP);
        TextView tvCoins = findViewById(R.id.tvCoins);
        StatusBarBinder.bind(this, tvXP, tvPP, tvCoins);


        levelAdvancementService = new LevelAdvancementService();
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        ArrayList<Level> levels = new ArrayList<>(db.levelRepository().getAll());
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        User user = db.userRepository().getByFirebaseUid(firebaseUser.getUid());

        ArrayList<LevelDTO> levelDTOs = levelAdvancementService.getLevelsForUser(levels, user);

        adapter = new LevelAdvancementAdapter(
                this,
                levelDTOs);

        ListView list = findViewById(R.id.listLevels);
        list.setAdapter(adapter);
    }
}