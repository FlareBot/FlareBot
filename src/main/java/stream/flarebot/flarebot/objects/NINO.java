package stream.flarebot.flarebot.objects;

import stream.flarebot.flarebot.util.RandomUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class NINO {

    private boolean enabled;
    private Set<String> whitelist;
    private String removeMessage;
    private List<String> removeMessages = new CopyOnWriteArrayList<>();

    public NINO() {
        this.enabled = false;
        this.whitelist = new HashSet<>();
        this.whitelist.add("discord.gg/discord-developers");
        this.whitelist.add("discord.gg/TTAUGvZ");
        this.removeMessage = "No no no, you post not an invite here!\nYes, hmmm.";
        this.removeMessages.add(removeMessage);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addInvite(String str) {
        this.whitelist.add(str);
    }

    public void removeInvite(String invite) {
        if (this.whitelist.contains(invite))
            this.whitelist.remove(invite);
    }

    public void setRemoveMessage(String str) {
        this.removeMessage = str;
        if (this.removeMessages.size() == 1)
            this.removeMessages.set(0, str);
        else
            this.removeMessages.add(str);
    }

    public void addRemoveMessage(String str) {
        this.removeMessages.add(str);
    }

    public void clearRemoveMessages() {
        this.removeMessage = null;
        this.removeMessages.clear();
    }

    @Nullable
    public String getRemoveMessage() {
        migrate();
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

    public void migrate() {
        if (removeMessage != null && removeMessages.isEmpty())
            removeMessages.add(removeMessage);
    }
}
