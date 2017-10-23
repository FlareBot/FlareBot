package stream.flarebot.flarebot.commands.automod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

public class ModlogCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (!getPermissions(channel).hasPermission(member, "flarebot.modlog"))
            return;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("setchannel")) {
                guild.getAutoModConfig()
                        .setModLogChannel(channel.getId());
                channel.sendMessage(new EmbedBuilder().setColor(Color.green)
                        .setDescription("The modlog channel has been changed to " + channel
                                .getAsMention()).build()).queue();
            }
            /*} else if (args[0].equalsIgnoreCase("config")) {
            } else if (args[0].equalsIgnoreCase("set")) {

            } else {
                MessageUtils.sendErrorMessage("Invalid argument!", channel);
            }
        } else if (args.length == 2) {

        } else if (args.length == 3) {*/

        } else {
            MessageUtils.sendUsage(this, channel, sender, args);
        }
    }

    @Override
    public String getCommand() {
        return "modlog";
    }

    @Override
    public String getDescription() {
        return "Set the channel where mod events can be logged.";
    }

    @Override
    public String getUsage() {
        return "`{%}modlog setchannel` - Set the modlog to be displayed in this channel.\n";
                /*+ "`{%}modlog config` - View the config of the modlog.\n"
                + "`{%}modlog set <config_option> <value>` - Set config options";*/
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}
