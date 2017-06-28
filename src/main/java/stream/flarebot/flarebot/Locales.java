package stream.flarebot.flarebot;

public enum Locales {

    ENGLISH_UK("en_uk"),
    ENGLISH_US("en_us"),
    FRENCH("fr");

    private String code;

    Locales(String code) {
        this.code = code;
    }

    public static Locales from(String s) {
        for (Locales l : Locales.values()) {
            if (s.equalsIgnoreCase(l.getCode()))
                return l;
        }
        throw new IllegalArgumentException("Unknown language code (" + s + ") !");
    }

    public String getCode() {
        return code;
    }
}
