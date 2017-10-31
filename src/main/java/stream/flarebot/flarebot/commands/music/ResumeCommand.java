package stream.flarebot.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

public class ResumeCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        PlayerManager musicManager = FlareBot.getInstance().getMusicManager();
        if (musicManager.getPlayer(channel.getGuild().getId()).getPlayingTrack() == null &&
                (musicManager.getPlayer(channel.getGuild().getId()).getPaused())) {
            MessageUtils.sendErrorMessage("There is no music playing!", channel);
        } else {
            musicManager.getPlayer(channel.getGuild().getId()).play();
            channel.sendMessage("Resuming...!").queue();
        }
    }

    @Override
    public String getCommand() {
        return "resume";
    }

    @Override
    public String getDescription() {
        return "Resumes your playlist";
    }

    @Override
    public String getUsage() {
        return "`{%}resume` - Resumes the playlist";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

}
