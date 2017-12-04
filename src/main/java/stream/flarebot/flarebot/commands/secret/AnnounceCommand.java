package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;

public class AnnounceCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message msg, String[] args, Member member) {
        if(guild.getGuildId().equals("226785954537406464")) {
            Role r = guild.getGuild().getRoleById(320304080926801922L);
            r.getManager().setMentionable(true).complete();
            String message = msg.getRawContent();
            message = message.substring(message.indexOf(" ") + 1);
            guild.getGuild().getTextChannelById(226786449217945601L).sendMessage(r.getAsMention() + "\n" + message).complete();
            r.getManager().setMentionable(false).queue();
        }
    }

    @Override
    public String getCommand() {
        return "announcement";
    }

    @Override
    public String getDescription() {
        return "Dev only command";
    }

    @Override
    public String getUsage() {
        return "{%}announcement [message]";
    }

    @Override
    public CommandType getType() {
        return CommandType.SECRET;
    }
}
