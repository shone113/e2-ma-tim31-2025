package ftn.project.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.data.dto.UserEquipmentDTO;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.BattleStartService;
import ftn.project.domain.usecase.EquipmentActivationService;
import ftn.project.presentation.adapter.EquipmentActivationAdapter;

public class EquipmentActivationActivity extends AppCompatActivity {

    private EquipmentActivationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_equipment_activation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        EquipmentActivationService equipmentActivationService = new EquipmentActivationService(db);

        GridView gvItems = findViewById(R.id.gvEquipment);
        Button btnConfirm = findViewById(R.id.btnConfirm);
        Button btnCancel = findViewById(R.id.btnCancel);

        User loggedUser = db.userRepository().getByFirebaseUid(firebaseUser.getUid());
        ArrayList<UserEquipmentDTO> userEquipmentDTOs = equipmentActivationService.getEquipmentForUser(loggedUser.getUserId());
        Log.w("EQUIP_ACT", "Broj opreme: " + userEquipmentDTOs.size());

        adapter = new EquipmentActivationAdapter(
                this,
                userEquipmentDTOs,
                firebaseUser.getUid(),
                this::performActivation
        );

        gvItems.setAdapter(adapter);

        // === Dugme "Confirm" — pokreće borbu ===
        btnConfirm.setOnClickListener(v -> {
            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                User currentUser = db.userRepository().getByFirebaseUid(fbUser.getUid());
                if (currentUser == null) {
                    runOnUiThread(() -> Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show());
                    return;
                }

                BattleStartService starter = new BattleStartService(this);
                BattleStartService.BattleStartResult result = starter.startNewBattle(currentUser);

                runOnUiThread(() -> {
                    Intent intent = new Intent(this, BattleActivity.class);
                    intent.putExtra("battleId", result.battleId);
                    intent.putExtra("hitChance", result.hitChance);
                    startActivity(intent);
                    finish();
                });
            });
        });

        // === Dugme "Cancel" — vraća korisnika nazad ===
        btnCancel.setOnClickListener(v -> {
            finish(); // samo zatvara ovu aktivnost
        });
    }

    private void performActivation(int userEquipmentId) {
        FirebaseUser fb = FirebaseAuth.getInstance().getCurrentUser();
        if (fb == null) {
            startActivity(new Intent(this, AuthActivity.class));
            return;
        }

        String uid = fb.getUid();
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            User u = db.userRepository().getByFirebaseUid(uid);

            if (u == null) {
                runOnUiThread(() ->
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            db.runInTransaction(() -> {
                db.userEquipmentRepository().activateEquipment(userEquipmentId);
            });

            runOnUiThread(() ->
                    Toast.makeText(this, "Activated!", Toast.LENGTH_SHORT).show()
            );
        });
    }
}
