package ftn.project.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import ftn.project.R;
import ftn.project.data.database.FirestoreSync;
import ftn.project.data.db.AppDatabase;
import ftn.project.data.dto.UserEquipmentDTO;
import ftn.project.domain.entity.Equipment;
import ftn.project.domain.entity.User;
import ftn.project.domain.entity.UserEquipment;
import ftn.project.domain.usecase.EquipmentActivationService;
import ftn.project.presentation.adapter.EquipmentActivationAdapter;
import ftn.project.presentation.adapter.ShopAdapter;

public class EquipmentActivationActivity extends AppCompatActivity {

    private ArrayList<UserEquipment> userEquipment;
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

        User loggedUser = db.userRepository().getByFirebaseUid(firebaseUser.getUid());
        ArrayList<UserEquipmentDTO> userEquipmentDTOs = equipmentActivationService.getEquipmentForUser(loggedUser.getUserId());
        Log.w("COUF", "" + userEquipmentDTOs.stream().count());

        adapter = new EquipmentActivationAdapter(
                this,
                userEquipmentDTOs,
                firebaseUser.getUid(),
                (userEquipmentId) -> performActivation(userEquipmentId));

        gvItems.setAdapter(adapter);
    }

    private void performActivation(int userEquipmentId) {
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

            // 1) Room transakcija
            db.runInTransaction(() -> {
                db.userEquipmentRepository().activateEquipment(userEquipmentId);
            });

            runOnUiThread(() ->
                    Toast.makeText(this, "Activated!", Toast.LENGTH_SHORT).show()
            );

        });
    }
}