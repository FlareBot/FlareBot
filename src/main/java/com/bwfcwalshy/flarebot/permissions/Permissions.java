package com.bwfcwalshy.flarebot.permissions;

import com.bwfcwalshy.flarebot.FlareBot;
import net.dv8tion.jda.core.entities.*;

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

    public PerGuildPermissions getPermissions(MessageChannel channel) {
        if (channel.getType() != ChannelType.TEXT)
            return new PrivateChannelPermission(channel);
        return perms.computeIfAbsent(((TextChannel) channel).getGuild().getId(), PerGuildPermissions::new);
    }
}
