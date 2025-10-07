package ftn.project.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ftn.project.R;
import ftn.project.domain.entity.Category;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnEditClickListener {
        void onEditClick(Category category);
    }

    private List<Category> categoryList;
    private OnEditClickListener listener;

    public CategoryAdapter(List<Category> categoryList, OnEditClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.bind(category, listener);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void updateCategories(List<Category> newCategories) {
        this.categoryList = newCategories;
        notifyDataSetChanged();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {

        TextView tvCategoryName;
        View viewColor;
        Button btnEditCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            viewColor = itemView.findViewById(R.id.viewColor);
            btnEditCategory = itemView.findViewById(R.id.btnEditCategory);
        }

        public void bind(Category category, OnEditClickListener listener) {
            tvCategoryName.setText(category.getName());
            viewColor.setBackgroundColor(category.getColor());

            btnEditCategory.setOnClickListener(v -> listener.onEditClick(category));
        }
    }
}
