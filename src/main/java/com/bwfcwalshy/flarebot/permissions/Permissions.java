package com.bwfcwalshy.flarebot.permissions;

import com.bwfcwalshy.flarebot.FlareBot;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.PrivateChannel;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

public class Permissions {
    private HashMap<String, PerGuildPermissions> perms = new HashMap<>();

    public void save() throws IOException {
        try (Writer fw = new FileWriter(FlareBot.PERMS_FILE)) {
            FlareBot.GSON.toJson(this, fw);
            fw.flush();
            fw.close();
        }
    }

    public PerGuildPermissions getPermissions(Channel channel) {
        if (channel instanceof PrivateChannel)
            return new PrivateChannelPermission(channel);
        return perms.computeIfAbsent(channel.getGuild().getId(), PerGuildPermissions::new);
    }
}
