package stream.flarebot.flarebot.mod.nino;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum URLCheckFlag {

    // Any sort of URL
    URL(false),
    // Any IP grabber URL we have blacklisted
    IP_GRABBER(true),
    // Discord Invites
    DISCORD_INVITE(true),
    // Phishing links which we have blacklisted
    PHISHING(true),
    // Screamers
    SCREAMERS(true),
    // NSFW content - This will only fire in non-NSFW channels.
    NSFW(true),

    // Any other blacklisted domains (This is a recommended flag!)
    // -- This is currently not implemented, will see in the future if it should be.
    //BLACKLISTED,

    // Any possible link which could lead to a phishing site or other.
    // This will blacklist things like cheap TLDs which are most used for spam and scams.
    // https://www.spamhaus.org/statistics/tlds/
    SUSPICIOUS(false);

    public static final URLCheckFlag[] values = values();
    private static ArrayList<URLCheckFlag> all;
    private static ArrayList<URLCheckFlag> defaults;

    private boolean defaultFlag;
    URLCheckFlag(boolean b) {
        this.defaultFlag = b;
    }

    public boolean isDefaultFlag() {
        return defaultFlag;
    }

    public static List<URLCheckFlag> getAllFlags() {
        if (all == null || all.size() == 0) {
            all = new ArrayList<>();
            all.addAll(Arrays.asList(values));
        }
        return all;
    }

    public static List<URLCheckFlag> getDebugFlags() {
        List<URLCheckFlag> flags = getAllFlags();
        flags.remove(URLCheckFlag.URL);
        return flags;
    }

    public static List<URLCheckFlag> getDefaults() {
        if (defaults == null || defaults.size() == 0) {
            defaults = new ArrayList<>();
            for (URLCheckFlag flag : values) {
                if (flag.isDefaultFlag())
                    defaults.add(flag);
            }
        }
        return defaults;
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
