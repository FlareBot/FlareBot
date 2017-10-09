package stream.flarebot.flarebot.util;

import java.awt.Color;

public enum MessageType {

    INFO(Color.CYAN),
    SUCCESS(Color.GREEN),
    WARNING(Color.YELLOW),
    MODERATION(Color.WHITE),
    ERROR(Color.RED),
    NEUTRAL(null);

    private final Color color;

    MessageType(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
