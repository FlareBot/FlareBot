package stream.flarebot.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Player;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;

public class LoopCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        Player player = FlareBot.instance().getMusicManager().getPlayer(channel.getGuild().getId());
        if (!player.getLooping()) {
            player.setLooping(true);
            channel.sendMessage("Looping: **ON**").queue();
        } else {
            player.setLooping(false);
            channel.sendMessage("Looping: **OFF**").queue();
        }
    }

    @Override
    public String getCommand() {
        return "loop";
    }

    @Override
    public String getDescription() {
        return "Toggles looping of the current playlist";
    }

    @Override
    public String getUsage() {
        return "`{%}loop` - Toggles looping of current playlist.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"andagainandagainandagainandagain"};
    }
}
