package stream.flarebot.flarebot.mod.nino;

import org.apache.commons.lang3.text.WordUtils;

public enum URLCheckFlag {

    // Any sort of URL
    URL(false),
    // Any IP grabber URL we have blacklisted
    IP_GRABBER(true),
    // Discord Invites
    DISCORD_INVITE(true),
    // Phishing links which we have blacklisted
    PHISHING(true),

    // Any other blacklisted domains (This is a recommended flag!)
    // -- This is currently not implemented, will see in the future if it should be.
    //BLACKLISTED,

    // Any possible link which could lead to a phishing site or other.
    // This will blacklist things like cheap TLDs which are most used for spam and scams.
    // https://www.spamhaus.org/statistics/tlds/
    SUSPICIOUS(false);

    public static URLCheckFlag[] values = values();

    private boolean defaultFlag;
    URLCheckFlag(boolean b) {
        this.defaultFlag = b;
    }

    public boolean isDefaultFlag() {
        return defaultFlag;
    }

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(name().replace("_", " "));
    }

    public static URLCheckFlag getFlag(String s) {
        for (URLCheckFlag flag : values) {
            if (s.equalsIgnoreCase(flag.toString()) || s.replace(" ", "_").equalsIgnoreCase(flag.name()))
                return flag;
        }
        return null;
    }
}
