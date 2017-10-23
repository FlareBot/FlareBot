package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.ISnowflake;
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
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StatsCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            EmbedBuilder bld = MessageUtils.getEmbed(sender)
                    .setThumbnail(MessageUtils.getAvatar(channel.getJDA().getSelfUser()));
            bld.setDescription("FlareBot v" + FlareBot.getInstance().getVersion() + " stats");
            for (Content content : Content.values) {
                bld.addField(content.getName(), content.getReturn(), content.isAlign());
            }
            channel.sendMessage(bld.build()).queue();
        } else {
            String search = FlareBot.getMessage(args);

            for (Content content : Content.values) {
                if (search.equalsIgnoreCase(content.getName()) || search.replaceAll("_", " ")
                        .equalsIgnoreCase(content.getName())) {
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .addField(content.getName(), content.getReturn(), false).build())
                            .queue();
                    return;
                }
            }
            MessageUtils.sendErrorMessage("That piece of information could not be found!", channel);
        }
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
        return "`{%}stats [section]` - Sends stats about the bot";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    public enum Content {
        SERVERS("Servers", () -> String.valueOf(FlareBot.getInstance().getGuilds().size())),
        TOTAL_USERS("Total Users", () -> String.valueOf(Arrays.stream(FlareBot.getInstance().getClients())
                .flatMap(c -> c.getUsers().stream())
                .map(ISnowflake::getId)
                .collect(Collectors.toSet()).size())),
        VOICE_CONNECTIONS("Voice Connections", () -> String
                .valueOf(FlareBot.getInstance().getConnectedVoiceChannels().size())),
        ACTIVE_CHANNELS("Channels Playing Music", () -> String
                .valueOf(FlareBot.getInstance().getActiveVoiceChannels())),
        TEXT_CHANNELS("Text Channels", () -> String.valueOf(FlareBot.getInstance().getChannels().size())),
        LOADED_GUILDS("Loaded Guilds", () -> String.valueOf(FlareBotManager.getInstance().getGuilds().size())),
        UPTIME("Uptime", () -> FlareBot.getInstance().getUptime()),
        MEM_USAGE("Memory Usage", () -> getMb(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())),
        MEM_FREE("Memory Free", () -> getMb(Runtime.getRuntime().freeMemory())),
        VIDEO_THREADS("Video Threads", () -> String.valueOf(VideoThread.VIDEO_THREADS.activeCount())),
        TOTAL_THREADS("Total Threads", () -> String.valueOf(Thread.getAllStackTraces().size()));

        private String name;
        private Supplier<String> returns;
        private boolean align = true;

        public static Content[] values = values();

        Content(String name, Supplier<String> returns) {
            this.name = name;
            this.returns = returns;
        }

        public String getName() {
            return name;
        }

        public String getReturn() {
            return returns.get();
        }

        public boolean isAlign() {
            return this.align;
        }
    }
}
