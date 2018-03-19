package stream.flarebot.flarebot.mod.nino;

import org.apache.commons.lang3.text.WordUtils;

public enum URLCheckFlag {

    // Any sort of URL
    URL,
    // Any IP grabber URL we have blacklisted
    IP_GRABBER,
    // Discord Invites
    DISCORD_INVITE,
    // Phishing links which we have blacklisted
    PHISHING,
    // Any other blacklisted domains (This is a recommended flag!)
    BLACKLISTED,
    // Any possible link which could lead to a phishing site or other.
    // This will blacklist things like cheap TLDs which are most used for spam and scams.
    // https://www.spamhaus.org/statistics/tlds/
    SUSPICIOUS;

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(name().replace("_", " "));
    }
}
