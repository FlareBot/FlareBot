package stream.flarebot.flarebot.util;

import java.awt.Color;
import java.util.Optional;

public enum ColorEnum {
    WHITE(255, 255, 255),
    LIGHT_GRAY(192, 192, 192),
    GRAY(128, 128, 128),
    DARK_GRAY(64, 64, 64),
    BLACK(0, 0, 0),
    RED(255, 0, 0),
    PINK(255, 175, 175),
    ORANGE(255, 200, 0),
    YELLOW(255, 255, 0),
    GREEN(0, 255, 0),
    MAGENTA(255, 0, 255),
    CYAN(0, 255, 255),
    BLUE(0, 0, 255);


    private int r;
    private int g;
    private int b;

    ColorEnum(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public static Optional<Color> getColorByName(String name) {
        for (ColorEnum colorEnum : ColorEnum.values()) {
            if (name.equalsIgnoreCase(colorEnum.name().trim())) {
                return Optional.of(new Color(colorEnum.r, colorEnum.g, colorEnum.b));
            }
        }
        return Optional.empty();
    }

}