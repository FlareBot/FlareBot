package stream.flarebot.flarebot.util;

public class Welcome {

    private String guildId;
    private String channelId;
    private String welcomeMessage;

    public Welcome(String guildId, String channelId) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.welcomeMessage = "Welcome %user% to %guild%";
    }

    public String getGuildId() {
        return this.guildId;
    }

    public String getChannelId() {
        return this.channelId;
    }

    public Welcome setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getMessage() {
        return this.welcomeMessage;
    }

    public Welcome setMessage(String msg) {
        this.welcomeMessage = msg;
        return this;
    }
}
