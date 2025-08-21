package ftn.project.presentation.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Equipment;
import ftn.project.presentation.adapter.ShopAdapter;
import ftn.project.presentation.adapter.UserAdapter;

public class ShopActivity extends AppCompatActivity {

    private ArrayList<Equipment> equipment;
    private ShopAdapter adapter;

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

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        String path = db.getOpenHelper().getWritableDatabase().getPath();
        android.util.Log.d("DB", "Opened DB at: " + path);
        equipment = new ArrayList<>();

        GridView gvItems = findViewById(R.id.gvShop);

        equipment = new ArrayList<>(db.equipmentRepository().getAll());
        Log.d("SHOP", "Equipment count = " + equipment.size());
        adapter = new ShopAdapter(this, equipment, eq -> {
            Toast.makeText(this,
                    "Dodao prijatelja:",
                    Toast.LENGTH_SHORT).show();
        });

        gvItems.setAdapter(adapter);
    }
}