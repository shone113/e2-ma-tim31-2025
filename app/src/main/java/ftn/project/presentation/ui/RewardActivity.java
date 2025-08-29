package ftn.project.presentation.ui;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

import java.util.concurrent.TimeUnit;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Battle;
import ftn.project.domain.entity.Boss;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.BossService;

import nl.dionsegijn.konfetti.xml.KonfettiView;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;

public class RewardActivity extends AppCompatActivity {

    private LottieAnimationView chestAnimation;
    private LinearLayout rewardContainer;
    private TextView coinsText;
    private ImageView equipmentView;
    private KonfettiView konfettiView;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener shakeListener;

    private boolean chestOpened = false;

    private int earnedCoins;
    private String equipmentIcon;

    private int battleId;
    private int hitChance;

    private boolean nextBattleChecked = false;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);

        chestAnimation = findViewById(R.id.chestAnimation);
        rewardContainer = findViewById(R.id.rewardContainer);
        coinsText = findViewById(R.id.coinsText);
        equipmentView = findViewById(R.id.equipmentView);
        konfettiView = findViewById(R.id.konfettiView);

        db = AppDatabase.getInstance(this);

        // Preuzmi nagrade iz Intenta
        earnedCoins = getIntent().getIntExtra("coins", 0);
        equipmentIcon = getIntent().getStringExtra("equipment");
        battleId = getIntent().getIntExtra("battleId", -1);
        hitChance = getIntent().getIntExtra("hitChance", -1);

        // Setup senzora
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        shakeListener = new SensorEventListener() {
            private static final float SHAKE_THRESHOLD = 15f;
            private long lastTime = 0;

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (chestOpened) return;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

                long currentTime = System.currentTimeMillis();
                if (acceleration > SHAKE_THRESHOLD && (currentTime - lastTime) > 1000) {
                    lastTime = currentTime;
                    openChest();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
    }

    private void openChest() {
        chestOpened = true;
        chestAnimation.playAnimation();

        // Opcioni zvuk
        MediaPlayer mp = MediaPlayer.create(this, R.raw.chest_open);
        if (mp != null) {
            mp.setOnCompletionListener(MediaPlayer::release);
            mp.start();
        }

        chestAnimation.addAnimatorUpdateListener(valueAnimator -> {
            if (valueAnimator.getAnimatedFraction() > 0.7f) {
                showRewards();
            }
        });
    }

    private void showRewards() {
        rewardContainer.setVisibility(LinearLayout.VISIBLE);
        coinsText.setText("+" + earnedCoins + " coins");

        if (equipmentIcon != null && !equipmentIcon.isEmpty()) {
            equipmentView.setVisibility(ImageView.VISIBLE);
            int resId = getResources().getIdentifier(equipmentIcon, "drawable", getPackageName());
            if (resId != 0) {
                equipmentView.setImageResource(resId);
            } else {
                equipmentView.setImageResource(R.drawable.sword); // fallback
            }
        } else {
            equipmentView.setVisibility(ImageView.GONE);
        }

        // Konfete 🎉
        konfettiView.setVisibility(KonfettiView.VISIBLE);
        EmitterConfig emitterConfig = new Emitter(2, TimeUnit.SECONDS).perSecond(50);
        Party party = new PartyFactory(emitterConfig)
                .angle(Angle.BOTTOM)
                .spread(360)
                .position(new Position.Relative(0.5, 1.0))
                .build();
        konfettiView.start(party);
        konfettiView.postDelayed(this::checkForNextBattle, 2500);
    }

    private void checkForNextBattle() {
        if (nextBattleChecked) return;
        nextBattleChecked = true;

        if (battleId == -1) {
            finish();
            return;
        }

        Battle finishedBattle = db.battleRepository().getBattleById(battleId);
        if (finishedBattle == null || !finishedBattle.isFinished()) {
            finish();
            return;
        }

        User user = db.userRepository().getById(finishedBattle.getUserId());
        int userLevel = user.getLevel();
        BossService bossService = new BossService(this);

        if (finishedBattle.isVictory()) {
            Boss nextBoss = bossService.getOrCreateBossForLevel(userLevel);
            if (nextBoss.getId() != finishedBattle.getBossId()) {
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

        // Level up SAMO JEDNOM ✅
        db.userRepository().updateLevel(user.getUserId(), userLevel + 1);

        Intent backIntent = new Intent(this, MainActivity.class);
        startActivity(backIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(shakeListener);
    }
}
