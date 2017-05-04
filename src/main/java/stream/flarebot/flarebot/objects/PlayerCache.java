package stream.flarebot.flarebot.objects;

import java.time.LocalDateTime;

public class PlayerCache {

    private String userId;
    private LocalDateTime lastMessage;
    private String lastSpokeGuild;
    private LocalDateTime lastSeen;

    public PlayerCache(String userId, LocalDateTime lastMessage, String lastSpokeGuild, LocalDateTime lastSeen) {
        this.userId = userId;
        this.lastMessage = lastMessage;
        this.lastSpokeGuild = lastSpokeGuild;
        this.lastSeen = lastSeen;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(LocalDateTime lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastSpokeGuild() {
        return lastSpokeGuild;
    }

    public void setLastSpokeGuild(String lastSpokeGuild) {
        this.lastSpokeGuild = lastSpokeGuild;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
}
