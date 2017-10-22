package stream.flarebot.flarebot;

import io.github.binaryoverload.JSONConfig;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Lang {

    private static Map<Locale, JSONConfig> languages = new HashMap<>();

    public void initLang() {
        try {
            long a = System.currentTimeMillis();
            loadLang(Locale.ENGLISH_UK, "en_uk");
            loadLang(Locale.GERMAN, "de");
            loadLang(Locale.FRENCH, "fr");
            FlareBot.LOGGER.info("Loaded " + languages.size() + " languages!");
        } catch (IOException e) {
            FlareBot.LOGGER.error("Failed to load langs!", e);
        }
    }

    private void loadLang(Locale locale, String langCode) throws IOException {
        URL url = ClassLoader.getSystemClassLoader().getResource("lang/" + langCode + ".json");
        if (url == null) {
            if (locale.mainLanguage) {
                FlareBot.LOGGER.error("Could not load the main language file! Due to this FlareBot could not start");
                System.exit(1);
            } else
                FlareBot.LOGGER.warn("Could not load the language " + locale.toString() + " check if the "
                        + locale.getLangCode() + ".json file exists!");
        } else
            languages.put(locale, new JSONConfig(url.openStream()));
    }

    /**
     * The "root" get method, everything should link into this one for a prefix conversion and general checking of paths.
     *
     * @param guild The GuildWrapper which requested this lang String. null for the default prefix.
     * @param path  Path in the language file to the desired text value.
     * @param args  The args for if the String needs formatting. Leave as nothing for a standard string.
     * @return The string from the desired language file with a formatted prefix.
     */
    public static String get(GuildWrapper guild, String path, Object... args) {
        Objects.requireNonNull(guild, "The GuildWrapper is required for translations!");
        if (languages.containsKey(guild.getL())) {
            if (languages.get(guild.getL()).getString(path).isPresent()) {
                return String.format(GeneralUtils.formatCommandPrefix(guild,
                        languages.get(guild.getL()).getString(path).get()), args);
            } else {
                if (guild.getL().mainLanguage)
                    throw new IllegalStateException("Cannot find '" + path + "' in the main language file! ("
                            + guild.getL().langCode + ")");
                FlareBot.LOGGER.warn("Cannot find a language string matching the path '" + path + "'");
                return null;
            }
        } else {
            throw new IllegalStateException("The language file matching locale '" + guild.getL().langCode
                    + "' does not exist or is not loaded!");
        }
    }

    public static String get(GuildWrapper guild, Command cmd, String path, Object... args) {
        return get(guild, "commands." + cmd.getCommand() + "." + path, args);
    }

    public enum Locale {

        ENGLISH_UK("en_uk", true),
        GERMAN("de", false),
        FRENCH("fr", false);

        private String langCode;
        private boolean mainLanguage;

        Locale(String langCode, boolean mainLang) {
            this.langCode = langCode;
            this.mainLanguage = mainLang;
        }

        public String getLangCode() {
            return langCode;
        }

        public static Locale getLang(String arg) {
            for (Locale locale : values())
                if (locale.toString().equalsIgnoreCase(arg) || locale.langCode.equalsIgnoreCase(arg))
                    return locale;
            return null;
        }

        public static Locale getMainLanguage() {
            return Locale.ENGLISH_UK;
        }
    }
}
