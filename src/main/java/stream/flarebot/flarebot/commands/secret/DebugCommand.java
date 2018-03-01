package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.text.WordUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.web.DataInterceptor;

public class DebugCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length < 1) {
            MessageUtils.sendUsage(this, channel, sender, args);
            return;
        }
        FlareBot fb = FlareBot.getInstance();
    
        EmbedBuilder eb = MessageUtils.getEmbed();
        if (args[0].equalsIgnoreCase("flarebot") || args[0].equalsIgnoreCase("bot")) {
            eb.setTitle("Bot Debug").setDescription(String.format("Debug for FlareBot " + fb.getVersion()
                    + "\nGuilds: %d"
                    + "\nVoice Channels: %d"
                    + "\nActive Voice Channels: %d",
                    fb.getGuildsCache().size(), fb.getConnectedVoiceChannels(), fb.getActiveVoiceChannels()));
            
            StringBuilder sb = new StringBuilder();
            for (DataInterceptor interceptor : DataInterceptor.getInterceptors())
                sb.append(WordUtils.capitalize(interceptor.name())).append(" - ").append(interceptor.getRequests())
                        .append(" requests");
            eb.addField("HTTP requests", sb.toString(), false);
        }
        
        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public String getCommand() {
        return "debug";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getUsage() {
        return "{%}debug flarebot|server|player";
    }

    @Override
    public CommandType getType() {
        return CommandType.SECRET;
    }
}
