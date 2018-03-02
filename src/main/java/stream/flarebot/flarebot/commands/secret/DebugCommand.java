package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.text.WordUtils;
import stream.flarebot.flarebot.Events;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.music.VideoThread;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;
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
            eb.setTitle("Bot Debug").setDescription(String.format("Debug for FlareBot v" + fb.getVersion()
                    + "\nUptime: %s"
                    + "\nMemory Usage: %s"
                    + "\nMemory Free: %s"
                    + "\nVideo Threads: %d"
                    + "\nCommand Threads: %d"
                    + "Total Threads: %d"
                    + "\n\nGuilds: %d"
                    + "\nLoaded Guilds: %d"
                    + "\nVoice Channels: %d"
                    + "\nActive Voice Channels: %d"
                    + "\nCommands Executed: %d",

                    fb.getUptime(),
                    getMB(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()),
                    getMB(Runtime.getRuntime().freeMemory()),
                    VideoThread.VIDEO_THREADS.activeCount(),
                    Events.COMMAND_THREADS.activeCount(),
                    Thread.getAllStackTraces().size(),
                    fb.getGuildsCache().size(), 
                    FlareBotManager.getInstance().getGuilds().size(), 
                    fb.getConnectedVoiceChannels(), 
                    fb.getActiveVoiceChannels(),
                    fb.getEvents().getCommandCount()
            ));
            
            StringBuilder sb = new StringBuilder();
            for (DataInterceptor interceptor : DataInterceptor.getInterceptors())
                sb.append(WordUtils.capitalize(interceptor.getSender().getName())).append(" - ").append(interceptor.getRequests())
                        .append(" requests").append("\n");
            eb.addField("HTTP Requests", sb.toString(), false);
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
    
    private String getMB(long bytes) {
        return (bytes / 1024 / 1024) + "MB";
    }
}
