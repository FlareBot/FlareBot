package stream.flarebot.flarebot.objects;

import org.eclipse.jetty.util.ConcurrentHashSet;
import stream.flarebot.flarebot.mod.nino.NINOMode;
import stream.flarebot.flarebot.mod.nino.URLCheckFlag;
import stream.flarebot.flarebot.util.RandomUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NINO {

    // 0 - Relaxed (Check for protocol and don't follow URLs)
    // 1 - Passive (Check for protocol and follow URLs)
    // 2 - Aggressive (Ignore protocol and follow URLs)
    private byte mode = 0;

    private Set<String> whitelistedUrls;
    private Set<Long> whitelistedChannels;
    private List<String> removeMessages = new CopyOnWriteArrayList<>();

    private Set<URLCheckFlag> urlFlags = new ConcurrentHashSet<>();

    public NINO() {
        this.whitelistedUrls = new ConcurrentHashSet<>();
        this.whitelistedChannels = new ConcurrentHashSet<>();
        this.whitelistedUrls.add("discord.gg/discord-developers");
        this.whitelistedUrls.add("discord.gg/TTAUGvZ");
        this.removeMessages.add("No no no, you post not an invite here!\nYes, hmmm.");
    }

    public boolean isEnabled() {
        return !urlFlags.isEmpty();
    }

    public void addUrl(String str) {
        this.whitelistedUrls.add(str);
    }

    public void removeUrl(String url) {
        if (this.whitelistedUrls.contains(url))
            this.whitelistedUrls.remove(url);
    }

    public void addChannel(long channelId) {
        this.whitelistedChannels.add(channelId);
    }

    public void removeChannel(long channelId) {
        if (this.whitelistedChannels.contains(channelId))
            this.whitelistedChannels.remove(channelId);
    }

    public void setRemoveMessage(String str) {
        if (this.removeMessages.size() == 1)
            this.removeMessages.set(0, str);
        else
            this.removeMessages.add(str);
    }

    public void addRemoveMessage(String str) {
        this.removeMessages.add(str);
    }

    public void clearRemoveMessages() {
        this.removeMessages.clear();
    }

    @Nullable
    public String getRemoveMessage() {
        if (this.removeMessages.isEmpty())
            return null;
        return "[NINO] " + RandomUtils.getRandomString(removeMessages);
    }

    public Set<String> getWhitelist() {
        return whitelistedUrls;
    }

    public List<String> getRemoveMessages() {
        return removeMessages;
    }

    public Set<URLCheckFlag> getURLFlags() {
        return urlFlags;
    }

    public void addURLFlags(URLCheckFlag flag, URLCheckFlag... flags) {
        this.getURLFlags().add(flag);
        if (flags.length > 0)
            this.getURLFlags().addAll(Arrays.asList(flags));
    }

    public Set<Long> getChannels() {
        return whitelistedChannels;
    }

    public byte getMode() {
        return mode;
    }

    @Nonnull
    public NINOMode getNINOMode() {
        NINOMode mode = NINOMode.getModeByByte(this.mode);
        if (mode == null) {
            mode = NINOMode.RELAXED;
            this.mode = mode.getMode();
        }
        return mode;
    }

    public void setMode(byte b) {
        this.mode = b;
    }
}
