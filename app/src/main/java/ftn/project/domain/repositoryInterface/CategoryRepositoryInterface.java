package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.project.domain.entity.Category;

@Dao
public interface CategoryRepositoryInterface {
    @Insert
    void insert(Category category);

    @Query("SELECT * FROM categories")
    List<Category> getAll();

    @Update
    void update(Category category);
    @Query("SELECT c.* FROM categories c WHERE c.id = :categoryId")
    Category getCategory(int categoryId);
}
