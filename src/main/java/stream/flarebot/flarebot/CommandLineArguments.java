package stream.flarebot.flarebot;

import org.apache.commons.cli.*;

public enum CommandLineArguments {
    TOKEN("Log in token", "t", "token", "token"),
    SQL_PW("MySQL login password for user flare at localhost for database FlareBot", "sql", "sql", "password"),
    SECRET("Webhooks secret", "s", "secret", "secret"),
    DBOTS("Discord Bots token", "db", "discord-bots-token", "discord-bots"),
    BOTLIST("Botlist token", "bt", "botlist", "botlist-token"),
    YTAPI("YouTube search API token", "yt", "yt-api-token", "yt-api-token"),
    WEBSECRET("Website API secret", "websecret", "web-secret", "web-secret"),
    STATUSHOOK("Bot status hook", "statushook", "statushook", "statushook"),
    TESTBOT("Is this a test bot?", "tb", "testbot", null),
    DEBUG("Debug", "debug", "debug", null);

    private Option option;
    private static CommandLine parsed;

    CommandLineArguments(String desc, String opt, String longOpt, String argName) {
        this(desc, opt, longOpt, argName, false);
    }

    CommandLineArguments(String desc, String opt, String longOpt, String argName, boolean required) {
        option = new Option(opt, argName != null, desc);
        if (argName != null) option.setArgName(argName);
        option.setLongOpt(longOpt);
        option.setRequired(required);
    }

    public String getValue() {
        return option.getValue();
    }

    public boolean isSet() {
        return parsed.hasOption(option.getOpt());
    }

    static void parse(String... args) throws Exception {
        Options options = new Options();

        try {
            for (CommandLineArguments a : values()) {
                options.addOption(a.option);
            }

            CommandLineParser p = new DefaultParser();
            parsed = p.parse(options, args);
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar FlareBot.jar", "FlareBot", options, "https://github.com/FlareBot/FlareBot", true);
            throw e;
        }
    }
}
