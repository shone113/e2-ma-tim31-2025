package ftn.project.presentation.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executors;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Category;
import ftn.project.presentation.adapter.CategoryAdapter;
import yuku.ambilwarna.AmbilWarnaDialog;

public class CategoryListActivity extends AppCompatActivity {

    private Category editingCategory = null;
    private LinearLayout layoutNewCategory;
    private EditText etCategoryName;
    private Button btnPickColor, btnSaveCategory, btnNewCategoryDialog;
    private RecyclerView recyclerViewCategories;
    private int chosenColor;
    private AppDatabase db;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_list);

        layoutNewCategory = findViewById(R.id.layoutNewCategory);
        etCategoryName = findViewById(R.id.etCategoryName);
        btnPickColor = findViewById(R.id.btnPickColor);
        btnSaveCategory = findViewById(R.id.btnSaveCategory);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        btnNewCategoryDialog = findViewById(R.id.btnAddCategory);

        db = AppDatabase.getInstance(this);
        chosenColor = Color.BLUE;
        btnPickColor.setBackgroundColor(chosenColor);


        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(
                List.of(),
                category -> openCategoryFormForEdit(category)
        );
        recyclerViewCategories.setAdapter(adapter);

        loadCategories();
        btnPickColor.setOnClickListener(v -> openColorPicker());
        btnSaveCategory.setOnClickListener(v -> saveCategory());
        btnNewCategoryDialog.setOnClickListener(v -> {
            if (layoutNewCategory.getVisibility() == View.VISIBLE) {
                clearCategoryForm();
                btnNewCategoryDialog.setText("Dodaj kategoriju");
            } else {
                layoutNewCategory.setVisibility(View.VISIBLE);
                btnSaveCategory.setText("Dodaj"); //
                btnNewCategoryDialog.setText("Zatvori");
            }
        });
    }

    private void openColorPicker() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, chosenColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                chosenColor = color;
                btnPickColor.setBackgroundColor(chosenColor);
            }
            @Override
            public void onCancel(AmbilWarnaDialog dialog) { }
        });
        colorPicker.show();
    }

    private void openCategoryFormForEdit(Category category) {
        layoutNewCategory.setVisibility(View.VISIBLE);
        etCategoryName.setText(category.getName());
        chosenColor = category.getColor();
        btnPickColor.setBackgroundColor(chosenColor);
        editingCategory = category;
        btnSaveCategory.setText("Sacuvaj");
        btnNewCategoryDialog.setText("Zatvori");
    }
    private void clearCategoryForm() {
        etCategoryName.setText("");
        chosenColor = Color.BLUE;
        btnPickColor.setBackgroundColor(chosenColor);
        layoutNewCategory.setVisibility(View.GONE);
        editingCategory = null; // reset
    }


    private void saveCategory() {
        String name = etCategoryName.getText().toString();
        if (name.isEmpty()) {
            etCategoryName.setError("Unesi naziv kategorije");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Category> categories = db.categoryRepository().getAll();

            boolean colorExists = false;
            for (Category c : categories) {
                if (editingCategory != null && c.getId() == editingCategory.getId()) continue;
                if (c.getColor() == chosenColor) {
                    colorExists = true;
                    break;
                }
            }

            if (colorExists) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Boja je već korišćena! Izaberi drugu.", Toast.LENGTH_SHORT).show()
                );
                return;
            }
            if(editingCategory == null)
                addNewCategory(name,chosenColor);
            else
                editCategory(editingCategory,name,chosenColor);

        });
    }

    private void addNewCategory(String name, int color){
            Category category = new Category(name, color);
            db.categoryRepository().insert(category);

            runOnUiThread(() -> {
                Toast.makeText(this, "Kategorija sačuvana!", Toast.LENGTH_SHORT).show();
                clearCategoryForm();
                loadCategories();
            });
    }
    private void editCategory(Category category, String name, int color)
    {
        category.setName(name);
        category.setColor(color);
        db.categoryRepository().update(category);
        runOnUiThread(() -> {
            Toast.makeText(this, "Kategorija ažurirana!", Toast.LENGTH_SHORT).show();
            clearCategoryForm();
            loadCategories();
        });
    }

    private void loadCategories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Category> categories = db.categoryRepository().getAll();
            runOnUiThread(() -> adapter.updateCategories(categories)); //zbog refreshovanja
        });
    }
}
