package stream.flarebot.flarebot.objects;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Welcome {

    @Expose(serialize = false, deserialize = false)
    private Random random = new Random();

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
        return dmMessages.get(random.nextInt(dmMessages.size()));
    }

    public String getRandomGuildMessage() {
        return guildMessages.get(random.nextInt(guildMessages.size()));
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
