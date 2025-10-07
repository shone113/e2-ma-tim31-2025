package ftn.project.domain.entity;

import androidx.annotation.Nullable;

public enum Badge {
    BADGE_1("BADGE_1", "Sova Školarac", "badge_1"),
    BADGE_2("BADGE_2", "Sova Avijatičar", "badge_2"),
    BADGE_3("BADGE_3", "Sova Putnik", "badge_3"),
    BADGE_4("BADGE_4", "Sova Sportista", "badge_4"),
    BADGE_5("BADGE_5", "Sova Muzikant", "badge_5"),
    BADGE_6("BADGE_6", "Sova Umjetnik", "badge_6"),
    BADGE_7("BADGE_7", "Sova Naučnik", "badge_7"),
    BADGE_8("BADGE_8", "Sova Programer", "badge_8"),
    BADGE_9("BADGE_9", "Sova Istraživač", "badge_9"),
    BADGE_10("BADGE_10", "Sova Pobednik", "badge_10");

    private final String code;
    private final String title;
    private final String imageName;


    Badge(String code, String title, String imageName) {
        this.code = code; this.title = title; this.imageName = imageName;
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getImageName() { return imageName; }

    public static @Nullable Badge byCode(String code){
        for (Badge b : values()) if (b.code.equals(code)) return b;
        return null;
    }
}
