package ftn.project.presentation.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Battle;
import ftn.project.domain.entity.Boss;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.BattleService;

public class BattleActivity extends AppCompatActivity {

    private ProgressBar bossHpBar, userPpBar;
    private TextView chanceToHitText, attacksLeftText, bossTitle, userPpText, rewardCoins;
    private Button attackButton;
    private ImageView bossImageView;

    private Boss currentBoss;
    private Battle currentBattle;

    private int userPp;  // TODO: povuci iz User entiteta
    private int hitChance;

    private BattleService battleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boss_fight);

        initViews();
        battleService = new BattleService(this);

        int battleId = getIntent().getIntExtra("battleId", -1);
        hitChance = getIntent().getIntExtra("hitChance", -1);
        if (battleId == -1) {
            Toast.makeText(this, "Greška: battleId nije prosleđen", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        AppDatabase db = AppDatabase.getInstance(this);
        currentBattle = db.battleRepository().getBattleById(battleId);
        if (currentBattle == null) {
            Toast.makeText(this, "Greška: nema battle-a u bazi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentBoss = db.bossRepository().getBossById(currentBattle.getBossId());
        if (currentBoss == null) {
            Toast.makeText(this, "Greška: nema bossa u bazi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String firebaseUid = firebaseUser.getUid();
        User currentUser = db.userRepository().getByFirebaseUid(firebaseUid);
        userPp = currentUser.getPowerPoints();
        if(userPp == 0)
            userPp = 50;
        setupUi();

        attackButton.setOnClickListener(v -> {
            Log.d("BattleActivity", "Attack button clicked!");
            battleService.performAttack(
                    currentBattle,
                    currentBoss,
                    userPp,
                    hitChance,
                    this::refreshUi
            );
        });
    }

    private void initViews() {
        bossHpBar = findViewById(R.id.bossHpBar);
        userPpBar = findViewById(R.id.userPpBar);
        chanceToHitText = findViewById(R.id.chanceToHit);
        attacksLeftText = findViewById(R.id.attacksLeft);
        bossTitle = findViewById(R.id.bossTitle);
        userPpText = findViewById(R.id.userPP);
        attackButton = findViewById(R.id.attackButton);
        bossImageView = findViewById(R.id.bossImage);
        rewardCoins = findViewById(R.id.rewardCoins);
    }

    private void setupUi() {
        bossHpBar.setMax(currentBoss.getMaxHp());
        bossHpBar.setProgress(currentBoss.getHp());

        userPpBar.setMax(200);
        userPpBar.setProgress(userPp);

        bossTitle.setText("Fight Lvl " + currentBoss.getLevel());
        userPpText.setText("PP " + userPp);

        rewardCoins.setText(String.valueOf(currentBoss.getCoinReward()));

        chanceToHitText.setText(hitChance + "%");
        attacksLeftText.setText(String.valueOf(currentBattle.getAttacksRemaining()));
        int imageResId = getResources().getIdentifier(
                currentBoss.getBossImage(), // npr. "boss_2"
                "drawable",
                getPackageName()
        );
        if (imageResId != 0) {
            bossImageView.setImageResource(imageResId);
        } else {
            bossImageView.setImageResource(R.drawable.boss_2); // fallback ako nema
        }
    }

    /**
     * Poziva se iz BattleService-a na glavnoj niti posle svakog napada
     */
    private void refreshUi() {
        bossHpBar.setProgress(currentBoss.getHp());
        attacksLeftText.setText(String.valueOf(currentBattle.getAttacksRemaining()));

        if (currentBattle.isFinished() || currentBattle.getAttacksRemaining() == 0) {
            attackButton.setEnabled(false);
        }
    }
}
