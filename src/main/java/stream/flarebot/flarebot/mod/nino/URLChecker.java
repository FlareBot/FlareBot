package stream.flarebot.flarebot.mod.nino;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.Pair;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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

    private static final Pattern URL_PATTERN = Pattern.compile("(https?://)?(([\\w-]+)\\.)?([\\w-]+)(\\.[\\w+]+)+(/[\\w-]+(\\.[\\w+]{1,5})?)*");

    private static URLChecker instance;

    public static URLChecker instance() {
        if (instance == null)
            instance = new URLChecker();
        return instance;
    }

    public void checkMessage(GuildWrapper wrapper, String message, BiConsumer<URLCheckFlag, String> callback) {
        Matcher m = URL_PATTERN.matcher(message);
        if (!m.find()) return;
        EXECUTOR.submit(() -> {
            Set<URLCheckFlag> flags = wrapper.getNINO().getURLFlags();

            String url = normalizeUrl(m.group());
            Pair<URLCheckFlag, String> pair = checkURL(url, flags);

            if (pair != null) {
                callback.accept(pair.getKey(), pair.getValue());
                return;
            }else {
                if ((pair = followURL(url, flags, null)) != null) {
                    callback.accept(pair.getKey(), pair.getValue());
                    return;
                }
            }

            callback.accept(null, null);
        });
    }

    private Pair<URLCheckFlag, String> checkURL(String url, Set<URLCheckFlag> flags) {
        Matcher matcher;
        // Check whitelisted domains

        // Blacklist

        // IP Grabber
        if (flags.contains(URLCheckFlag.IP_GRABBER)) {
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
        if (normalized.contains(" ")) {
            logger.info(url);
        }

        if (!url.startsWith("http")) {
            normalized = "http://" + url;
        }

        return normalized.trim();
    }

    public void runTests() {
        List<String> tests = ImmutableList.of(
                "Check this out https://i.go.iplogger.com/12dxzfcs.php",
                "cool.webcam",
                "discord.gg/b1nzy",
                "flarebot.stream",
                "http://bit.ly/2Ix3h5k",
                "iplogger.com",
                "http://test.iplogger.com/t?=192.0.0.1"
        );

        GuildWrapper wrapper = new GuildWrapper("1");
        wrapper.getNINO().addURLFlags(URLCheckFlag.IP_GRABBER, URLCheckFlag.BLACKLISTED, URLCheckFlag.DISCORD_INVITE,
                URLCheckFlag.PHISHING, URLCheckFlag.SUSPICIOUS);

        for (String url : tests) {
            instance().checkMessage(wrapper, url, (flag, u) -> logger.info(url + " - " + flag));
        }
    }
}
