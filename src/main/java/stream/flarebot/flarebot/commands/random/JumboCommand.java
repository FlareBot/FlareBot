package stream.flarebot.flarebot.commands.random;

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class JumboCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length > 0) {
            if (!message.getEmotes().isEmpty() && message.getEmotes().size() <= 5) {
                if (message.getEmotes().size() == 1) {
                    Emote e = message.getEmotes().get(0);
                    channel.sendMessage("**Name: **" + e.getName()
                            + "\n**ID: **" + e.getId()
                            + "\n**Server: **" + (e.getGuild() != null ? e.getGuild().getName()
                            + " (" + e.getGuild().getId() + ")": "Unknown")
                            + "\n**Link: **" + e.getImageUrl()).queue();
                    return;
                }

                StringBuilder sb = new StringBuilder();
                //MessageAction action = channel.sendMessage(MessageUtils.ZERO_WIDTH_SPACE);
                for (Emote e : message.getEmotes()) {
                    sb.append("**Name:** `:").append(e.getName()).append(":`  **Link: **").append(e.getImageUrl()).append("\n");
                    /*InputStream stream = read(e.getImageUrl(), channel);
                    if (stream != null)
                        action = action.append("**Name: ** ").append(e.getName()).append("  **Link:** <")
                                .append(e.getImageUrl()).append(">").append("\n")
                                .addFile(stream, e.getName() + (e.isAnimated() ? ".gif" : ".png"));*/
                        /*channel.sendFile(stream, e.getName() + (e.isAnimated() ? ".gif" : ".png"),
                                new MessageBuilder().setContent("<" + e.getImageUrl() + ">").build()).queue()*/
                }
                //action.queue();
                channel.sendMessage(sb.toString()).queue();
            } else
                MessageUtils.sendWarningMessage("Please send a valid emote or send a maximum of 5!", channel);
        } else
            MessageUtils.sendUsage(this, channel, sender, args);
    }

    @Override
    public String getCommand() {
        return "jumbo";
    }

    @Override
    public String getDescription() {
        return "Jumbo size an emote!";
    }

    @Override
    public String getUsage() {
        return "`{%}jumbo <emote>` - See an emote in it's true size because who doesn't love jumbo sized stuff?";
    }

    @Override
    public CommandType getType() {
        return CommandType.RANDOM;
    }

    private InputStream read(String url, TextChannel channel) {
        try {
            HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla/5.0 FlareBot");

            return conn.getInputStream();
        } catch (IOException e) {
            MessageUtils.sendErrorMessage("Failed to jumbo image!\nMessage: " + e.getMessage(), channel);
            FlareBot.LOGGER.error("Failed to send image for " + getCommand() + " command. Guild ID: "
                    + channel.getGuild().getId() + ", URL: " + url, e);
            return null;
        }
    }
}
