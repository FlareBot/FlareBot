package stream.flarebot.flarebot.mod.nino;

import stream.flarebot.flarebot.util.general.GeneralUtils;

public enum NINOMode {

    RELAXED((byte) 0, "Check for protocol and not follow URLs"),
    PASSIVE((byte) 1, "Check for protocol and follow URLs"),
    AGGRESSIVE((byte) 2, "Ignore protocol and follow URLs");

    private byte mode;
    private String explanation;
    NINOMode(byte b, String explanation) {
        this.mode = b;
        this.explanation = explanation;
    }

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public byte getMode() {
        return mode;
    }

    public String getExplanation() {
        return explanation;
    }

    public static NINOMode getMode(String s) {
        byte b = GeneralUtils.getByte(s, (byte) -1);
        for (NINOMode modes : values()) {
            if (modes.getMode() == b || modes.name().equalsIgnoreCase(s))
                return modes;
        }
        return null;
    }

    public static NINOMode getModeByByte(byte mode) {
        for (NINOMode modes : values()) {
            if (modes.getMode() == mode)
                return modes;
        }
        return null;
    }
}
