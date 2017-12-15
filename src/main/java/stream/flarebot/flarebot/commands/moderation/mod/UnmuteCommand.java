package stream.flarebot.flarebot.commands.moderation.mod;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

public class UnmuteCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length != 1) {
            MessageUtils.sendUsage(this, channel, sender, args);
        } else {
            User user = GeneralUtils.getUser(args[0], guild.getGuildId());
            if (user == null) {
                MessageUtils.sendErrorMessage("Invalid user!!", channel);
                return;
            }
            if (guild.getMutedRole() == null) {
                MessageUtils.sendErrorMessage("Error getting the \"Muted\" role! Check FlareBot has permissions to create it!", channel);
                return;
            }


        }
    }

    @Override
    public String getCommand() {
        return "unmute";
    }

    @Override
    public String getDescription() {
        return "Unmutes a user";
    }

    @Override
    public String getUsage() {
        return "`{%}unmute <user>` - Unmutes a user.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}
