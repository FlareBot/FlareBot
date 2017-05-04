package stream.flarebot.flarebot.permissions;

import stream.flarebot.flarebot.FlareBot;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;

public class Permissions {
    private ConcurrentHashMap<String, PerGuildPermissions> perms = new ConcurrentHashMap<>();

    public void save() throws IOException {
        try (Writer fw = new FileWriter(FlareBot.PERMS_FILE)) {
            FlareBot.GSON.toJson(this, fw);
            fw.flush();
            fw.close();
        }
    }

    public PerGuildPermissions getPermissions(MessageChannel channel) {
        if (channel.getType() != ChannelType.TEXT)
            return new PrivateChannelPermission(channel);
        return perms.computeIfAbsent(((TextChannel) channel).getGuild().getId(), PerGuildPermissions::new);
    }
}
