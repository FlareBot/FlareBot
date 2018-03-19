package stream.flarebot.flarebot.mod.nino;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.Pair;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLChecker {

    private static final Logger logger = LoggerFactory.getLogger(URLChecker.class);

    private static final ThreadGroup GROUP = new ThreadGroup("URLChecker");
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4, r ->
            new Thread(GROUP, r, "Checker-" + GROUP.activeCount()));

    private static final ConcurrentHashMap<UUID, Byte> redirects = new ConcurrentHashMap<>();

    private static final Pattern URL_PATTERN = Pattern.compile("(?:https?://|www\\.)([^\\s/$.?#].[^\\s]*)");

    private static URLChecker instance;

    public static URLChecker instance() {
        if (instance == null)
            instance = new URLChecker();
        return instance;
    }

    public void checkMessage(GuildWrapper wrapper, String message, BiConsumer<URLCheckFlag, String> callback) {
        Matcher m = URL_PATTERN.matcher(message);
        if (!m.find()) return;

        String url = normalizeUrl(m.group(1));
        /*if (!validURL(url))
            return;*/

        EXECUTOR.submit(() -> {
            Set<URLCheckFlag> flags = wrapper.getNINO().getURLFlags();

            Pair<URLCheckFlag, String> pair = checkURL(url, flags);

            if (pair != null) {
                // Returned if it's a whitelisted URL
                if (pair.getKey() == null && pair.getValue() != null) return;
                logger.info("{} was found to be under the flag {} ({})", url, pair.getKey(), pair.getValue());
                callback.accept(pair.getKey(), pair.getValue());
                return;
            } else {
                logger.debug("{} was not flagged, going to try and follow the URL.", url);
                if ((pair = followURL(url, flags, null)) != null) {
                    logger.debug("{} was found to be under the flag {} ({}) after following it", url, pair.getKey(), pair.getValue());
                    callback.accept(pair.getKey(), pair.getValue());
                    return;
                }
            }

            callback.accept(null, null);
        });
    }

    private Pair<URLCheckFlag, String> checkURL(String url, Set<URLCheckFlag> flags) {
        Matcher matcher;
        logger.debug("Checking {} with flags: {}", url, Arrays.toString(flags.toArray()));
        // Check whitelisted domains
        if ((matcher = URLConstants.WHITELISTED_DOMAINS_PATTERN.matcher(url)).find()) {
            return new Pair<>(null, matcher.group());
        }

        // Blacklist
        // I may want to implement this in the future but right now I don't think it's needed.

        // IP Grabber
        if (flags.contains(URLCheckFlag.IP_GRABBER)) {
            logger.debug(URLConstants.IP_GRABBER_PATTERN.toString());
            if ((matcher = URLConstants.IP_GRABBER_PATTERN.matcher(url)).find()) {
                return new Pair<>(URLCheckFlag.IP_GRABBER, matcher.group());
            }
        }

        // Discord Invite
        if (flags.contains(URLCheckFlag.DISCORD_INVITE)) {
            if ((matcher = URLConstants.DISCORD_INVITE_PATTERN.matcher(url)).find()) {
                return new Pair<>(URLCheckFlag.DISCORD_INVITE, matcher.group());
            }
        }

        // Phishing
        if (flags.contains(URLCheckFlag.PHISHING)) {
            if ((matcher = URLConstants.PHISHING_PATTERN.matcher(url)).find()) {
                return new Pair<>(URLCheckFlag.PHISHING, matcher.group());
            }
        }

        // Suspicious TLDs
        if (flags.contains(URLCheckFlag.SUSPICIOUS)) {
            if ((matcher = URLConstants.SUSPICIOUS_TLDS_PATTERN.matcher(url)).find()) {
                return new Pair<>(URLCheckFlag.SUSPICIOUS, matcher.group());
            }
        }

        // URL
        if (flags.contains(URLCheckFlag.URL)) {
            return new Pair<>(URLCheckFlag.URL, url);
        }

        return null;
    }

    private Pair<URLCheckFlag, String> followURL(String url, Set<URLCheckFlag> flags, UUID uuid) {
        UUID redirectUUID = uuid;
        if (uuid != null && redirects.containsKey(uuid)) {
            if (redirects.get(uuid) == 10) // Have a fallback so we don't follow a redirect loop forever.
                return null;
            else
                redirectUUID = UUID.randomUUID();
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.connect();

            int resp = connection.getResponseCode();
            String location = connection.getHeaderField("Location");

            if (location != null) {
                logger.info("{} ({}) wants to redirect to {}", url, resp, location);

                Pair<URLCheckFlag, String> pair = checkURL(location, flags);
                if (pair != null)
                    return pair;

                redirects.put(redirectUUID, (byte) (uuid != null ? redirects.get(uuid) + 1 : 1));
                return followURL(location, flags, redirectUUID);
            } else
                return null;
        } catch (IOException e) {
            logger.error("Failed to follow URL! URL: " + url, e);
            return null;
        }
    }

    private String normalizeUrl(String url) {
        String normalized = url;
        if (!url.startsWith("http")) {
            normalized = "http://" + url;
        }

        return normalized.trim();
    }

    public void runTests() {
        List<String> tests = ImmutableList.of(
                "Check this out https://i.go.iplogger.com/12dxzfcs.php",
                "http://cool.webcam",
                "http://www.discord.gg/b1nzy",
                "https://flarebot.stream",
                //"http://bit.ly/2Ix3h5k",
                "www.iplogger.com",
                "http://test.iplogger.com/t?=192.0.0.1",
                "http://bit.ly/2FY9rJW"
        );

        GuildWrapper wrapper = new GuildWrapper("1");
        wrapper.getNINO().addURLFlags(URLCheckFlag.IP_GRABBER, URLCheckFlag.BLACKLISTED, URLCheckFlag.DISCORD_INVITE,
                URLCheckFlag.PHISHING, URLCheckFlag.SUSPICIOUS);

        for (String url : tests) {
            instance().checkMessage(wrapper, url, (flag, u) -> logger.info(url + " - " + flag));
        }
    }
}
