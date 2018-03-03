package stream.flarebot.flarebot.objects;

import stream.flarebot.flarebot.util.RandomUtils;

import java.util.HashSet;
import java.util.Set;

public class NINO {

    private boolean enabled;
    private Set<String> whitelist;
    private String removeMessage;
    private Set<String> removeMessages = new HashSet<>();

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
    }

    public String getRemoveMessage() {
        migrate();
        return RandomUtils.getRandomString(removeMessages);
    }

    public Set<String> getWhitelist() {
        return whitelist;
    }

    public void migrate() {
        if (removeMessages.isEmpty())
            removeMessages.add(removeMessage);
    }
}
