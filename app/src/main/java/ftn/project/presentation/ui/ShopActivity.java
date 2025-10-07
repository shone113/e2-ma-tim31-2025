package ftn.project.presentation.ui;

import android.app.StatusBarManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import ftn.project.R;
import ftn.project.data.database.FirestoreSync;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Equipment;
import ftn.project.domain.entity.SpecialMission;
import ftn.project.domain.entity.SpecialMissionProgress;
import ftn.project.domain.entity.User;
import ftn.project.domain.entity.UserEquipment;
import ftn.project.domain.usecase.SpecialMissionProgressService;
import ftn.project.presentation.adapter.ShopAdapter;
import ftn.project.presentation.adapter.UserAdapter;
import ftn.project.presentation.util.ImageResId;
import ftn.project.presentation.util.StatusBarBinder;

public class ShopActivity extends AppCompatActivity {

    private ArrayList<Equipment> equipment;
    private ShopAdapter adapter;
    private SpecialMissionProgressService specialMissionProgressService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvXP = findViewById(R.id.tvXP);
        TextView tvPP = findViewById(R.id.tvPP);
        TextView tvCoins = findViewById(R.id.tvCoins);
        ImageView ivTitle = findViewById(R.id.ivTitle);
        StatusBarBinder.bind(this, ivTitle, tvXP, tvPP, tvCoins);

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        equipment = new ArrayList<>();
        specialMissionProgressService = new SpecialMissionProgressService(
                db.specialMissionRepository(),
                db.specialMissionProgressRepository(),
                db.taskInstanceRepository(),
                db.allianceMessageRepository()
        );

        GridView gvItems = findViewById(R.id.gvShop);

        equipment = new ArrayList<>(db.equipmentRepository().getPurchasable());
        adapter = new ShopAdapter(
                this,
                equipment,
                firebaseUser.getUid(),
                (user, price, equipment) -> showConfirmPurchase(user, price, equipment));

        gvItems.setAdapter(adapter);
    }

    private void showConfirmPurchase(User user, double price, Equipment equipment) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_shop_confirm, null, false);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvBody  = view.findViewById(R.id.tvBody);
        ImageView ivArt  = view.findViewById(R.id.ivArt);
        MaterialButton btnBuy = view.findViewById(R.id.btnBuy);
        ImageButton btnClose  = view.findViewById(R.id.btnClose);

        String effectType;
        switch (equipment.getEffectType()){
            case STRENGTH:
                effectType = " PP";
                break;
            case ATTACK_CHANCE:
                effectType = " Attack success";
                break;
            case EXTRA_ATTACK:
                effectType = " Attack chance";
                break;
            default:
                effectType = "";
        }
        tvTitle.setText("+ " + Math.round(equipment.getBonusPercentage()) + "% " + effectType);

        int resId = ImageResId.returnResId(ShopActivity.this, equipment.getImageName());
        ivArt.setImageResource(resId);

        btnBuy.setText(String.valueOf(Math.round(price)));
        btnBuy.setIconResource(R.drawable.coin);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));
        }

        btnBuy.setOnClickListener(v -> {
            performPurchase(user, price, equipment);
            dialog.dismiss();
        });
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private void performPurchase(User user, double price, Equipment equipment) {
        FirebaseUser fb = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (fb == null) {
            startActivity(new Intent(this, AuthActivity.class));
            return;
        }

        String uid = fb.getUid();

        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            User u = db.userRepository().getByFirebaseUid(uid);

            if (u == null) {
                runOnUiThread(() ->
                        android.widget.Toast.makeText(this, "User profile not found", android.widget.Toast.LENGTH_SHORT).show()
                );
                return;
            }

            long coins = (user.getCoins() != null) ? u.getCoins() : 0L;

            if (coins < price) {
                runOnUiThread(() ->
                        android.widget.Toast.makeText(this, "Not enough coins", android.widget.Toast.LENGTH_SHORT).show()
                );
                return;
            }

            UserEquipment ue = new UserEquipment();
            ue.setUserId(u.getUserId());
            ue.setEquipmentId(equipment.getEquipmentId());
            ue.setBattleCount(equipment.getBattleCount());
            ue.setActive(equipment.getInitActiveType());

            // 1) Room transakcija
            db.runInTransaction(() -> {
                db.userRepository().subtractCoins(u.getUserId(), (long) price);
                db.userEquipmentRepository().add(ue);


                if(specialMissionProgressService.punchByShopping(u.getUserId()))
                {
                    SpecialMission specialMission = specialMissionProgressService.getActiveMission(u.getUserId());
                    if (specialMission != null) {
                        SpecialMissionProgress smp = specialMissionProgressService
                                .getActiveMissionProgress(specialMission.getId(), u.getUserId());

                        if (smp != null) {
                            smp.setShopPurchases(smp.getShopPurchases() + 1);
                            smp.setTotalDamage(smp.getTotalDamage() + 2);
                            db.specialMissionProgressRepository().update(smp);

                            specialMission.setBossHp(specialMission.getBossHp() - 2);
                            db.specialMissionRepository().update(specialMission);
                        }
                    }
                }
            });

            // 2) Firestore mirror – koristi ISTI ue i u.getUserId()
            FirestoreSync.mirrorUserEquipmentToFirestore(
                    getApplicationContext(),
                    uid,
                    u.getUserId(),
                    ue
            );

            runOnUiThread(() ->
                    Toast.makeText(this, "Purchased!", Toast.LENGTH_SHORT).show()
            );

        });
    }
}