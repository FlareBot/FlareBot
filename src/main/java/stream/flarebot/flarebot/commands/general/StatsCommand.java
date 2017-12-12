package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.music.VideoThread;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.implementations.MultiSelectionContent;

import java.awt.Color;
import java.util.function.Supplier;

public class StatsCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            EmbedBuilder bld = MessageUtils.getEmbed(sender).setColor(Color.CYAN)
                    .setThumbnail(MessageUtils.getAvatar(channel.getJDA().getSelfUser()));
            bld.setDescription("FlareBot v" + FlareBot.getInstance().getVersion() + " stats");
            for (MultiSelectionContent<String, String, Boolean> content : Content.values) {
                bld.addField(content.getName(), content.getReturn(), content.isAlign());
            }
            channel.sendMessage(bld.build()).queue();
        } else
            GeneralUtils.handleMultiSelectionCommand(sender, channel, args, Content.values);
    }

    private static String getMb(long bytes) {
        return (bytes / 1024 / 1024) + "MB";
    }

    @Override
    public String getCommand() {
        return "stats";
    }

    @Override
    public String getDescription() {
        return "Displays stats about the bot.";
    }

    @Override
    public String getUsage() {
        return "`{%}stats [section]` - Sends stats about the bot.";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    public enum Content implements MultiSelectionContent<String, String, Boolean> {

        SERVERS("Servers", () -> FlareBot.getInstance().getGuilds().size()),
        TOTAL_USERS("Total Users", () -> FlareBot.getInstance().getUsers().size()),
        VOICE_CONNECTIONS("Voice Connections", () -> FlareBot.getInstance().getConnectedVoiceChannels()),
        ACTIVE_CHANNELS("Channels Playing Music", () -> FlareBot.getInstance().getActiveVoiceChannels()),
        TEXT_CHANNELS("Text Channels", () -> FlareBot.getInstance().getChannels().size()),
        LOADED_GUILDS("Loaded Guilds", () -> FlareBotManager.getInstance().getGuilds().size()),
        COMMANDS_EXECUTED("Commands Executed", () -> FlareBot.getInstance().getEvents().getCommandCount()),
        UPTIME("Uptime", () -> FlareBot.getInstance().getUptime()),
        MEM_USAGE("Memory Usage", () -> getMb(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())),
        MEM_FREE("Memory Free", () -> getMb(Runtime.getRuntime().freeMemory())),
        VIDEO_THREADS("Video Threads", VideoThread.VIDEO_THREADS::activeCount),
        TOTAL_THREADS("Total Threads", () -> Thread.getAllStackTraces().size());

        private String name;
        private Supplier<Object> returns;
        private boolean align = true;

        public static Content[] values = values();

        Content(String name, Supplier<Object> returns) {
            this.name = name;
            this.returns = returns;
        }

        public String getName() {
            return name;
        }

        public String getReturn() {
            return String.valueOf(returns.get());
        }

        public Boolean isAlign() {
            return this.align;
        }
    }
}
