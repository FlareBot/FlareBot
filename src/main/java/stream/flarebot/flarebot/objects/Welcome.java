package stream.flarebot.flarebot.objects;

import stream.flarebot.flarebot.util.RandomUtils;

import java.util.ArrayList;
import java.util.List;

public class Welcome {

    private List<String> dmMessages;
    private List<String> guildMessages;
    private boolean dmEnabled = false;
    private boolean guildEnabled = false;
    private String channelId;

    public Welcome() {
        dmMessages = new ArrayList<>();
        guildMessages = new ArrayList<>();
        dmMessages.add("Welcome %user% to %guild%");
        guildMessages.add("Welcome %user% to %guild%");
    }

    public Welcome setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getChannelId() {
        return this.channelId;
    }

    public List<String> getDmMessages() {
        return this.dmMessages;
    }

    public List<String> getGuildMessages() {
        return this.guildMessages;
    }

    public String getRandomDmMessage() {
        return RandomUtils.getRandomString(dmMessages);
    }

    public String getRandomGuildMessage() {
        return RandomUtils.getRandomString(guildMessages);
    }

    public boolean isDmEnabled() {
        return this.dmEnabled;
    }

    public boolean isGuildEnabled() {
        return guildEnabled;
    }

    public void setDmEnabled(boolean enabled) {
        this.dmEnabled = enabled;
    }

    public void setGuildEnabled(boolean enabled) {
        this.guildEnabled = enabled;
    }
}
