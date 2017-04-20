package stream.flarebot.flarebot.commands.music;

import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.commands.FlareBotManager;
import stream.flarebot.flarebot.music.VideoThread;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.Set;
import java.util.stream.Collectors;

public class RandomCommand implements Command {

    private FlareBotManager manager = FlareBotManager.getInstance();

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length != 1) {
            loadSongs(25, channel, sender);
        } else {
            int amount;
            try {
                amount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription("Invalid amount!"), channel);
                return;
            }
            if (amount <= 0)
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription("Invalid amount!"), channel);
            loadSongs(amount, channel, sender);
        }
    }

    private void loadSongs(int amount, TextChannel channel, User sender) {
        Set<String> songs = manager.getRandomSongs(amount, channel);
        VideoThread.getThread(songs.stream()
                .collect(Collectors.joining(",")), channel, sender).start();
    }

    @Override
    public String getCommand() {
        return "random";
    }

    @Override
    public String getDescription() {
        return "Put random songs into your playlist.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}
