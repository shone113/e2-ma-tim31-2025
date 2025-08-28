package ftn.project.presentation.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
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
import java.util.concurrent.Executors;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Badge;
import ftn.project.domain.entity.Equipment;
import ftn.project.domain.entity.Title;
import ftn.project.domain.entity.User;
import ftn.project.domain.entity.UserBadge;
import ftn.project.domain.entity.UserEquipment;
import ftn.project.presentation.adapter.BadgeAdapter;
import ftn.project.presentation.adapter.EquipmentAdapter;
import ftn.project.presentation.util.ImageResId;

public class ProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "ftn.project.EXTRA_USER_ID";

    private BadgeAdapter badgeAdapter;
    private EquipmentAdapter equipmentAdapter;

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
        ImageView imgAvatar = findViewById(R.id.imgAvatar);
        TextView tvTitle = findViewById(R.id.tvTitle);
        ImageView imgTitle = findViewById(R.id.imgTitle);
        TextView tvBadgesTitle = findViewById(R.id.tvBadgesTitle);
        TextView tvLevelBox = findViewById(R.id.tvLevelBox);
        TextView tvPowerBox = findViewById(R.id.tvPowerBox);
        TextView tvExperienceBox = findViewById(R.id.tvExperienceBox);
        TextView tvCoinsBox = findViewById(R.id.tvCoinsBox);
        ImageView ivQR = findViewById(R.id.ivQR);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(!TextUtils.equals(user.getFirebaseUid(), firebaseUser.getUid())){
            tvPowerBox.setVisibility(View.GONE);
            tvCoinsBox.setVisibility(View.GONE);
            TextView tvCoinsLabel = findViewById(R.id.tvCoinsLabel);
            tvCoinsLabel.setVisibility(View.GONE);
            TextView tvPowerLabel = findViewById(R.id.tvPowerLabel);
            tvPowerLabel.setVisibility(View.GONE);
        }

        int resId = ImageResId.returnResId(this, user.getAvatarImage());
        imgAvatar.setImageResource(resId != 0 ? resId : R.drawable.potion);

        tvLevelBox.setText(String.valueOf(user.getLevel()));
        tvPowerBox.setText(user.getPowerPoints().toString());
        tvExperienceBox.setText(user.getExperiencePoints().toString());
        tvCoinsBox.setText(user.getCoins().toString());

        Title t = Title.fromLevel(user.getLevel());
        int nameId = getResources().getIdentifier(t.nameKey, "string", getPackageName());
        int iconId = getResources().getIdentifier(t.iconKey, "drawable", getPackageName());
        tvTitle.setText(nameId);
        imgTitle.setImageResource(iconId);

        if (db.userBadgeRepository().hasBadge(userId, "BADGE_1") == 0) {
            var ub = new UserBadge();
            ub.userId = userId;
            ub.badgeCode = "BADGE_1";
            db.userBadgeRepository().insert(ub);
        }
        if (db.userBadgeRepository().hasBadge(userId, "BADGE_2") == 0) {
            var ub = new UserBadge();
            ub.userId = userId;
            ub.badgeCode = "BADGE_2";
            db.userBadgeRepository().insert(ub);
        }

        List<String> ids = db.userBadgeRepository().getBadgeCodesForUser(userId);
        ArrayList<Badge> badges = new ArrayList<>();
        for (String id : ids) {
            Badge b = Badge.byCode(id);
            if (b != null) badges.add(b);
        }

        Log.w("BADGES", "" + db.userBadgeRepository().getAll().stream().count());

        GridView gvBadges = findViewById(R.id.gvBadges);
        badgeAdapter = new BadgeAdapter(this, badges);
        gvBadges.setAdapter(badgeAdapter);

        tvBadgesTitle.setText("Badges: " + badges.size());

        List<UserEquipment> userEquipment = db.userEquipmentRepository().getAllForUser(userId);
        ArrayList<Equipment> equipment = new ArrayList<>();
        for (UserEquipment ue : userEquipment) {
            Equipment e = db.equipmentRepository().getById(ue.getEquipmentId());
            if (e != null) equipment.add(e);
        }

        GridView gvEquipment = findViewById(R.id.gvEquipment);
        equipmentAdapter = new EquipmentAdapter(this, equipment);
        gvEquipment.setAdapter(equipmentAdapter);

        if(userId != -1){
            User u = AppDatabase.getInstance(this).userRepository().getById(userId);
            if(u != null){
                tvUsername.setText(u.getUsername());
            }
        }

        findViewById(R.id.ivQR).setOnClickListener(v -> showMyQrDialog(user));

    }

    private void showMyQrDialog(User me) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_profile_qr, null, false);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvSub   = view.findViewById(R.id.tvSub);
        ImageView ivQr   = view.findViewById(R.id.ivQr);
        com.google.android.material.button.MaterialButton btnClose = view.findViewById(R.id.btnClose);

        tvTitle.setText(me.getUsername() + "\'s QR code");

        try {
            org.json.JSONObject payload = new org.json.JSONObject();
            payload.put("app", "HabitQuest");
            payload.put("type", "add_friend");
            payload.put("v", 1);
            payload.put("uid", me.getFirebaseUid());
            payload.put("username", me.getUsername() != null ? me.getUsername() : "Player");

            android.graphics.Bitmap bmp = ftn.project.presentation.util.QrGenerator.generate(payload.toString(), 1024);
            ivQr.setImageBitmap(bmp);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Greška pri kreiranju QR koda", Toast.LENGTH_SHORT).show();
            return;
        }

        androidx.appcompat.app.AlertDialog dialog =
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setView(view)
                        .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

}