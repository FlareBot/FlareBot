package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.music.VideoThread;
import com.sun.management.OperatingSystemMXBean;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.*;
import spark.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.bwfcwalshy.flarebot.FlareBot.LOGGER;

public class InfoCommand implements Command {

    private static String git = null;

    static {
        if (new File("FlareBot" + File.separator).exists()) {
            try {
                ProcessBuilder p = new ProcessBuilder("git", "log", "--pretty=format:'%h'", "-n", "1");
                p.directory(new File("FlareBot" + File.separator));
                Process pr = p.start();
                pr.waitFor();
                if (pr.exitValue() == 0) {
                    git = IOUtils.toString(pr.getInputStream());
                }

            } catch (InterruptedException | IOException e1) {
                LOGGER.error("Could not compare git revisions!", e1);
            }
        }
    }

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            EmbedBuilder bld = MessageUtils.getEmbed(sender).setThumbnail(MessageUtils.getAvatar(channel.getJDA().getSelfUser()));
            bld.setDescription("FlareBot v" + FlareBot.getInstance().getVersion() + " info");
            for (Content content : Content.values) {
                bld.addField(content.getName(), content.getReturn(), content.isAlign());
            }
            sender.openPrivateChannel().complete().sendMessage(bld.build()).queue();
        } else {
            String search = "";
            for (String arg : args) {
                search += arg + " ";
            }
            search = search.trim();

            for (Content content : Content.values) {
                if (search.equalsIgnoreCase(content.getName()) || search.replaceAll("_", " ").equalsIgnoreCase(content.getName())) {
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .addField(content.getName(), content.getReturn(), false).build()).queue();
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
        return "info";
    }

    @Override
    public String getDescription() {
        return "Displays info about the bot.";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    public enum Content {
        SERVERS("Servers", () -> String.valueOf(FlareBot.getInstance().getGuilds().size())),
        TOTAL_USERS("Total Users", () -> String.valueOf(Arrays.stream(FlareBot.getInstance().getClients())
                .flatMap(c -> c.getUsers().stream()).map(ISnowflake::getId)
                .collect(Collectors.toSet()).size())),
        VOICE_CONNECTIONS("Voice Connections", () -> String.valueOf(FlareBot.getInstance().getConnectedVoiceChannels().size())),
        ACTIVE_CHANNELS("Channels Playing Music", () -> String.valueOf(FlareBot.getInstance().getActiveVoiceChannels())),
        TEXT_CHANNELS("Text Channels", () -> String.valueOf(FlareBot.getInstance().getChannels().size())),
        UPTIME("Uptime", () -> FlareBot.getInstance().getUptime()),
        MEM_USAGE("Memory Usage", () -> getMb(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())),
        MEM_FREE("Memory Free", () -> getMb(Runtime.getRuntime().freeMemory())),
        VIDEO_THREADS("Video Threads", () -> String.valueOf(VideoThread.VIDEO_THREADS.activeCount())),
        TOTAL_THREADS("Total Threads", () -> String.valueOf(Thread.getAllStackTraces().size())),
        VERSION("Version", FlareBot.getInstance().getVersion()),
        JDA_VERSION("JDA version", JDAInfo.VERSION),
        GIT("Git Revision", (git != null ? git : "Unknown")),
        CPU_USAGE("CPU Usage", () -> ((int) (ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getSystemCpuLoad() * 10000)) / 100f + "%"),
        SUPPORT_SERVER("Support Server", "[`Discord`](http://discord.me/flarebot)"),
        DONATIONS("Donate", "[`PayPal`](https://www.paypal.me/FlareBot/)"),
        PATREON("Our Patreon", "[`Patreon`](https://www.patreon.com/discordflarebot)"),
        WEBSITE("Website", "[`FlareBot`](http://flarebot.stream/)"),
        TWITTER("Twitter", "[`Twitter`](https://twitter.com/DiscordFlareBot)"),
        INVITE("Invite", String.format("[`Invite`](%s)", FlareBot.getInstance().getInvite())),
        EMPTY("\u200B", "\u200B", false),
        MADE_BY("Made By", "bwfcwalshy#1284 and Arsen#7525"),
        SOURCE("Source", "[`GitHub`](https://github.com/FlareBot/FlareBot)");

        private String name;
        private Supplier<String> returns;
        private boolean align = true;

        public static Content[] values = values();

        Content(String name, String returns) {
            this.name = name;
            this.returns = () -> returns;
        }

        Content(String name, String returns, boolean align) {
            this.name = name;
            this.returns = () -> returns;
            this.align = align;
        }

        Content(String name, Supplier<String> returns) {
            this.name = name;
            this.returns = returns;
        }

        Content(String name, Supplier<String> returns, boolean align) {
            this.name = name;
            this.returns = returns;
            this.align = align;
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
