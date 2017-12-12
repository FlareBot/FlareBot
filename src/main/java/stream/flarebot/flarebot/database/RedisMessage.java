package stream.flarebot.flarebot.database;

public class RedisMessage {

    private final String messageID;
    private final String authorID;
    private final String channelID;
    private final String guildID;
    private final String content;
    private final long timestamp;

    public RedisMessage(String messageID, String authorID, String channelID, String guildID, String content, long timestamp) {
        this.messageID = messageID;
        this.authorID = authorID;
        this.channelID = channelID;
        this.guildID = guildID;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getAuthorID() {
        return authorID;
    }

    public String getChannelID() {
        return channelID;
    }

    public String getGuildID() {
        return guildID;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
