package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.EnumSet;

public class PinCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1 && args[0].matches("[0-9]{18,22}")) {
            if (!guild.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE)) {
                MessageUtils.sendErrorMessage("I need the `Manage Messages` permission in order to pin!", channel);
                return;
            }

            Message msg = channel.getMessageById(args[0].trim()).complete();
            if (msg == null) {
                MessageUtils.sendErrorMessage("That message could not be found!", channel);
                return;
            }
            msg.pin().complete();
            channel.getHistory().retrievePast(1).complete().get(0).delete().queue();
        } else if (args.length != 0) {
            if (!guild.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE)) {
                MessageUtils.sendErrorMessage("I need the `Manage Messages` permission in order to pin!", channel);
                return;
            }

            Message msg = channel.sendMessage(new EmbedBuilder().setTitle(sender.getName(), null)
                    .setThumbnail(MessageUtils.getAvatar(sender)).setDescription(MessageUtils.getMessage(args, 0))
                    .build()).complete();
            msg.pin().complete();
            channel.getHistory().retrievePast(1).complete().get(0).delete().queue();
        } else {
            MessageUtils.sendUsage(this, channel, sender, args);
        }
    }

    @Override
    public String getCommand() {
        return "pin";
    }

    @Override
    public String getDescription() {
        return "Pin a message";
    }

    @Override
    public String getUsage() {
        return "`{%}pin <messageID|message>` - Pins a message either by ID or by typing a message";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }

    @Override
    public EnumSet<Permission> getDiscordPermission() {
        return EnumSet.of(Permission.MESSAGE_MANAGE);
    }
}
