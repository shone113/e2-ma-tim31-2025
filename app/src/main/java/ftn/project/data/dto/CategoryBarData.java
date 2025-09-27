package ftn.project.data.dto;

import java.util.List;

public class CategoryBarData {
    private final List<String> categories; // nazivi
    private final List<Integer> values;    // brojevi
    private final List<Integer> colors;    // ARGB boje

    public CategoryBarData(List<String> categories, List<Integer> values, List<Integer> colors) {
        this.categories = categories;
        this.values = values;
        this.colors = colors;
    }
    public List<String> getCategories() { return categories; }
    public List<Integer> getValues() { return values; }
    public List<Integer> getColors() { return colors; }
}
