package stream.flarebot.flarebot.mod.nino;

import com.google.common.collect.ImmutableSet;
import org.slf4j.LoggerFactory;
import stream.flarebot.flarebot.util.MessageUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class URLConstants {

    public static final Pattern URL_PATTERN = Pattern.compile("(https?://|www\\.)([\\w-#_?$]+\\.[^\\s]*)");
    public static final Pattern URL_PATTERN_NO_PROTOCOL = Pattern.compile("(https?://|www\\.)?([\\w-#_?$]+\\.[^\\s]*)");

    private static final Set<String> IP_GRABBERS = ImmutableSet.of(
            "iplogger.com",
            "iplogger.org",
            "blasze.tk",
            "yȯutube.com",
            "gyazo.nl",
            "i.imger.me",
            "mailble.com",
            "proxyfill.co",
            "steamtools.co",
            "grabify.link"
    );

    /**
     * Catch the nasty IP Grabbers which are listed above,
     */
    public static final Pattern IP_GRABBER_PATTERN = Pattern.compile(String.format("(%s)",
            IP_GRABBERS.stream().collect(Collectors.joining("|"))), Pattern.CASE_INSENSITIVE);

    public static final Pattern DISCORD_INVITE_PATTERN = Pattern
            .compile("(discord\\.gg|discordapp\\.com/invite)/[\\w-._]{2,32}", Pattern.CASE_INSENSITIVE);

    private static final Set<String> PHISHING = ImmutableSet.of(
            "paypal1.net-br.top",
            "netflixusersupport.sysvalidate.safeguard.billingdep011.com",
            "fortnitegiveaways.com"
    );

    public static Pattern PHISHING_PATTERN = Pattern.compile(String.format("(%s)",
            PHISHING.stream().collect(Collectors.joining("|"))), Pattern.CASE_INSENSITIVE);

    private static final Set<String> SUSPICIOUS_TLDS = ImmutableSet.of(
            "click",
            "men",
            "link",
            "party",
            "webcam",
            "work",
            "biz",
            "vip",
            "yokohama",
            "stream"
    );

    public static final Pattern SUSPICIOUS_TLDS_PATTERN = Pattern.compile(String.format("[\\w-]{1,256}\\.(%s)",
            SUSPICIOUS_TLDS.stream().collect(Collectors.joining("|"))), Pattern.CASE_INSENSITIVE);

    private static final Set<String> SCREAMERS = ImmutableSet.of(
            "akk.li",
            "dvorak.org",
            "ebaumsworld.com",
            "soulsphere.org"
    );
    
    public static final Pattern SCREAMERS_PATTERN = Pattern.compile(String.format("(%s)",
            SCREAMERS.stream().collect(Collectors.joining("|"))), Pattern.CASE_INSENSITIVE);

    private static final Set<String> NSFW = fetchNSFWSites();

    public static final Pattern NSFW_PATTERN = Pattern.compile(String.format("(%s)",
            NSFW.stream().collect(Collectors.joining("|"))), Pattern.CASE_INSENSITIVE);

    /**
     * These are domains which may be caught by our blacklisted TLDs or other such methods. These have been confirmed
     * safe and are all good to go to <3
     */
    private static final Set<String> WHITELISTED_DOMAINS = ImmutableSet.of(
            "flarebot.stream", // Our website :p

            "dankmeme.stream", // Image Server - https://nothing.domains/
            "hacked-your.webcam", // Image Server - https://nothing.domains/
            "jackloves.men", // Image Server - https://nothing.domains/
            "is-info.men", // Image Server - https://whats-th.is/faq.html
            "is-over.party", // Image Server - https://whats-th.is/faq.html
            "while-at.work" // Image Server - https://whats-th.is/faq.html
    );

    public static final Pattern WHITELISTED_DOMAINS_PATTERN = Pattern.compile(String.format("(%s)",
            WHITELISTED_DOMAINS.stream().collect(Collectors.joining("|"))), Pattern.CASE_INSENSITIVE);

    private static Set<String> fetchNSFWSites() {
        try {
            FileReader fr = new FileReader(new File("nsfw.txt"));
            BufferedReader br = new BufferedReader(fr);

            return ImmutableSet.copyOf(br.lines().collect(Collectors.toSet()));
        } catch (FileNotFoundException e) {
            LoggerFactory.getLogger(URLConstants.class).warn("Could not load NSFW list!!");
            return ImmutableSet.of();
        }
    }

    /**
     * Get the Discord invite from a String, this will return for either discord.gg or discordapp.com/invite
     * If you want one including a bunch of listing sites too then check {@link MessageUtils#getInvite(String)}
     *
     * @param str The string to check.
     * @return The invite link if found or null otherwise.
     */
    public static String getInvite(String str) {
        Matcher matcher = DISCORD_INVITE_PATTERN.matcher(str);
        if (matcher.find())
            return matcher.group();
        return null;
    }
}
