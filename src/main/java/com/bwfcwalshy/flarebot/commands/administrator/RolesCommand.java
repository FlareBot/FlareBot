package com.bwfcwalshy.flarebot.commands.administrator;

import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import net.dv8tion.jda.core.entities.*;

public class RolesCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Server Roles**\n```\n");
        for (Role role : channel.getGuild().getRoles()) {
            sb.append(role.getName()).append(" (").append(role.getId()).append(")\n");
        }
        sb.append("```");

        channel.sendMessage(sb.toString()).queue();
    }

    @Override
    public String getCommand() {
        return "roles";
    }

    @Override
    public String getDescription() {
        return "Get roles on the server";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public String getPermission() {
        return "flarebot.roles";
    }
}
