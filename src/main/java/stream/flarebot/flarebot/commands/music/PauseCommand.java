package stream.flarebot.flarebot.commands.music;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;

public class PauseCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        FlareBot.getInstance().getMusicManager().getPlayer(channel.getGuild().getId()).setPaused(true);
    }

    @Override
    public String getCommand() {
        return "pause";
    }

    @Override
    public String getDescription() {
        return "Pauses your song. Opposite of play";
    }

    @Override
    public String getUsage() {
        return "`{%}pause` - Pauses the currently playing song.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}
