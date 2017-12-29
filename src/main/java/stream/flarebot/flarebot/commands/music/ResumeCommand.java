package stream.flarebot.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Player;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.*;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

public class ResumeCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        Player player = FlareBot.getInstance().getMusicManager().getPlayer(guild.getGuildId());
        if (player.getPlayingTrack() == null) {
            MessageUtils.sendErrorMessage("There is no music playing!", channel);
        } else if (!player.getPaused()) {
            MessageUtils.sendErrorMessage("The music is only playing!", channel);
        } else {
            player.play();
            MessageUtils.sendSuccessMessage("Resuming...!", channel);
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
        return "`{%}resume` - Resumes the playlist.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

}
