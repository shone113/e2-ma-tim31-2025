package ftn.project.presentation.ui;

import android.app.Activity;
import android.content.Intent;
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
import ftn.project.domain.usecase.BossService;

public class BattleActivity extends AppCompatActivity {

    private ProgressBar bossHpBar, userPpBar;
    private TextView chanceToHitText, attacksLeftText, bossTitle, userPpText, rewardCoins;
    private Button attackButton;
    private ImageView bossImageView;

    private Boss currentBoss;
    private Battle currentBattle;

    private int userPp;
    private int hitChance;

    private BattleService battleService;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boss_fight);

        initViews();
        battleService = new BattleService(this);
        db = AppDatabase.getInstance(this);

        int battleId = getIntent().getIntExtra("battleId", -1);
        hitChance = getIntent().getIntExtra("hitChance", -1);
        if (battleId == -1) {
            Toast.makeText(this, "Greška: battleId nije prosleđen", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
                    this::onBattleUpdate
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
                currentBoss.getBossImage(),
                "drawable",
                getPackageName()
        );
        if (imageResId != 0) {
            bossImageView.setImageResource(imageResId);
        } else {
            bossImageView.setImageResource(R.drawable.boss_2); // fallback
        }
    }

    /**
     * Poziva se iz BattleService-a posle svakog napada
     */
    private void onBattleUpdate() {
        runOnUiThread(() -> {
            bossHpBar.setProgress(currentBoss.getHp());
            attacksLeftText.setText(String.valueOf(currentBattle.getAttacksRemaining()));

            if (currentBattle.isFinished() || currentBattle.getAttacksRemaining() == 0) {
                attackButton.setEnabled(false);

                // ✅ Kada je borba gotova, proveravamo šta dalje
                checkForNextBattle(currentBattle);
            }
        });
    }

    private void checkForNextBattle(Battle finishedBattle) {
        if (!finishedBattle.isFinished()) return;

        User user = db.userRepository().getById(finishedBattle.getUserId());
        int userLevel = user.getLevel();
        BossService bossService = new BossService(this);

        if (finishedBattle.isVictory()) {
            // Tražimo da li postoji još neki boss za isti level
            Boss nextBoss = bossService.getOrCreateBossForLevel(userLevel);

            if (nextBoss.getId() != finishedBattle.getBossId()) {
                // Ima sledećeg → otvaramo novu borbu
                Battle newBattle = new Battle(
                        0,
                        user.getUserId(),
                        nextBoss.getId(),
                        false,
                        0,
                        5,
                        null,
                        false
                );
                long id = db.battleRepository().insert(newBattle);

                Intent intent = new Intent(this, BattleActivity.class);
                intent.putExtra("battleId", (int) id);
                intent.putExtra("hitChance", hitChance);
                startActivity(intent);
                finish();
                return;
            }
        } else {
            bossService.ensureBossForLevel(userLevel);
        }

        db.userRepository().updateLevel(user.getUserId(), userLevel + 1);

        Toast.makeText(this, "Level up! Novi level: " + (userLevel + 1), Toast.LENGTH_SHORT).show();

        Intent backIntent = new Intent(this, TaskCalendarActivity.class);
        startActivity(backIntent);
        finish();
    }

}
