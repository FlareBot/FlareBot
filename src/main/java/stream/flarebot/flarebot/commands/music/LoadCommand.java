package stream.flarebot.flarebot.commands.music;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.music.VideoThread;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.List;

public class LoadCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender, args);
            return;
        }
        String name = MessageUtils.getMessage(args, 0);

        List<String> playlist = FlareBot.getInstance().getManager().loadPlaylist(channel, sender, name);
        if(!playlist.isEmpty())
            VideoThread.getThread(name + '\u200B' + playlist.toString(), channel, sender).start();


    }

    @Override
    public String getCommand() {
        return "load";
    }

    @Override
    public String getDescription() {
        return "Loads a playlist";
    }

    @Override
    public String getUsage() {
        return "`{%}load <playlist>` - Loads a playlist.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public String getPermission() {
        return "flarebot.queue.load";
    }
}
