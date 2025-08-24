package ftn.project.domain.entity;

public enum Title {
    OWL_EGG (1,  "owl_egg",  "title_1"),
    OWL_BABY(5,  "owl_baby", "title_2"),
    OWL_SCHOOL(5,  "owl_school", "title_3"),
    OWL_AVIATOR (10, "owl_aviator",  "title_4"),
    OWL_PEDIA  (20, "owl_pedia",   "title_5");

    public final int minLevel;
    public final String nameKey;   // ključ za string resurs
    public final String iconKey;   // ime drawable-a

    Title(int minLevel, String nameKey, String iconKey) {
        this.minLevel = minLevel;
        this.nameKey = nameKey;
        this.iconKey = iconKey;
    }

    public static Title fromLevel(int level){
        switch (level) {
            case 1:
                return OWL_EGG;
            case 2:
                return OWL_BABY;
            case 3:
                return OWL_SCHOOL;
            case 4:
                return OWL_AVIATOR;
            case 5:
                return OWL_PEDIA;
            default:
                return OWL_EGG;
        }
    }
}
