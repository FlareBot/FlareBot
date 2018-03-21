package stream.flarebot.flarebot.objects;

import org.eclipse.jetty.util.ConcurrentHashSet;
import stream.flarebot.flarebot.mod.nino.URLCheckFlag;
import stream.flarebot.flarebot.util.RandomUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class NINO {

    private Set<String> whitelist;
    private List<String> removeMessages = new CopyOnWriteArrayList<>();

    private Set<URLCheckFlag> urlFlags = new ConcurrentHashSet<>();

    public NINO() {
        this.whitelist = new HashSet<>();
        this.whitelist.add("discord.gg/discord-developers");
        this.whitelist.add("discord.gg/TTAUGvZ");
        this.removeMessages.add("No no no, you post not an invite here!\nYes, hmmm.");

        this.urlFlags.addAll(Arrays.stream(URLCheckFlag.values).filter(URLCheckFlag::isDefaultFlag)
                .collect(Collectors.toSet()));
    }

    public boolean isEnabled() {
        return !urlFlags.isEmpty();
    }

    public void addInvite(String str) {
        this.whitelist.add(str);
    }

    public void removeInvite(String invite) {
        if (this.whitelist.contains(invite))
            this.whitelist.remove(invite);
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
        return whitelist;
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

    public void resetFlags() {
        this.urlFlags.clear();
        this.urlFlags.addAll(URLCheckFlag.getDefaults());
    }
}
