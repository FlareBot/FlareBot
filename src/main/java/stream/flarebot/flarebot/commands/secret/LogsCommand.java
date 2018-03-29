package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.io.input.ReversedLinesFileReader;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.commands.InternalCommand;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.GeneralUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class LogsCommand implements InternalCommand {

    private final int DEFAULT_LINE_COUNT = 500;

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        int lineCount = DEFAULT_LINE_COUNT;
        if (args.length == 1) {
            lineCount = GeneralUtils.getInt(args[0], DEFAULT_LINE_COUNT);
        }

        try {
            ReversedLinesFileReader rlfr = new ReversedLinesFileReader(new File("latest.log"), Charset.forName("UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            for (int i = 0; i < lineCount; i++) {
                line = rlfr.readLine();
                if (line == null) break;
                sb.append(line).append("\n");
            }

            String pasteUrl = MessageUtils.paste(sb.toString());
            MessageUtils.sendPM(channel, sender, pasteUrl == null ? "null" : pasteUrl, "Could not DM you the logs! " +
                    "Please make sure the privacy settings allow me :( dis is pwivate, me need to send pwivately.");
        } catch (IOException e) {
            FlareBot.LOGGER.error("Failed to read latest.log", e);
            MessageUtils.sendException("Failed to read latest.log", e, channel);
        }
    }

    @Override
    public String getCommand() {
        return "logs";
    }

    @Override
    public String getDescription() {
        return "Gets the logs";
    }

    @Override
    public String getUsage() {
        return "{%}logs";
    }

    @Override
    public CommandType getType() {
        return CommandType.SECRET;
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }
}
