package stream.flarebot.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;

public class PauseCommand implements Command {

    private PlayerManager musicManager;

    public PauseCommand(FlareBot bot) {
        this.musicManager = bot.getMusicManager();
    }

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        musicManager.getPlayer(channel.getGuild().getId()).setPaused(true);
    }

    @Override
    public String getCommand() {
        return "pause";
    }

    @Override
    public String getDescription() {
        return "Pause your song. Opposite of play";
    }

    @Override
    public String getUsage() {
        return "{%}pause";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}
