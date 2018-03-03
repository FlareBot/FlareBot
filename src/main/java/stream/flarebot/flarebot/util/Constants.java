package stream.flarebot.flarebot.util;

import stream.flarebot.flarebot.FlareBot;

public class Constants {

    public static final String OFFICIAL_GUILD = "226785954537406464";
    private static final String FLAREBOT_API = "https://api.flarebot.stream";
    private static final String FLAREBOT_API_DEV = "http://localhost:8880";

    public static final long DEVELOPER_ID = 226788297156853771L;
    public static final long CONTRIBUTOR_ID = 272324832279003136L;
    public static final long STAFF_ID = 320327762881675264L;

    public static final String FLARE_TEST_BOT_CHANNEL = "242297848123621376";

    public static String getAPI() {
        return FlareBot.getInstance().isTestBot() ? FLAREBOT_API_DEV : FLAREBOT_API;
    }
}
