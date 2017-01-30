package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.music.VideoThread;
import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.io.IOUtils;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.EmbedBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;

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
                    git = IOUtils.toString(pr.getInputStream(), Charset.defaultCharset());
                }
            } catch (InterruptedException | IOException e1) {
                LOGGER.error("Could not compare git revisions!", e1);
            }
        }
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if(args.length == 0) {
            EmbedBuilder bld = MessageUtils.getEmbed(sender).withThumbnail(MessageUtils.getAvatar(channel.getClient().getOurUser()));
            bld.withDesc("FlareBot v" + FlareBot.getInstance().getVersion() + " info");
            for (Content content : Content.values) {
                bld.appendField(content.getName(), content.getReturn(), content.isAlign());
            }
            MessageUtils.sendMessage(bld, channel);
        }else{
            String search = "";
            for(int i = 0; i < args.length; i++){
                search += args[i] + " ";
            }
            search = search.trim();

            for(Content content : Content.values){
                if(search.equalsIgnoreCase(content.getName()) || search.replaceAll("_", " ").equalsIgnoreCase(content.getName())){
                    MessageUtils.sendMessage(MessageUtils.getEmbed(sender).appendField(content.getName(), content.getReturn(), false), channel);
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
        SERVERS("Servers", String.valueOf(FlareBot.getInstance().getClient().getGuilds().size())),
        VOICE_CONNECTIONS("Voice Connections", String.valueOf(FlareBot.getInstance().getClient().getGuilds().size())),
        ACTIVE_CHANNELS("Channels Playing Music", String.valueOf(FlareBot.getInstance().getClient().getConnectedVoiceChannels().stream()
                .map(IVoiceChannel::getGuild)
                .map(IDiscordObject::getID)
                .filter(gid -> FlareBot.getInstance().getMusicManager().hasPlayer(gid)).count())),
        TEXT_CHANNELS("Text Channels", String.valueOf(FlareBot.getInstance().getClient().getChannels(false).size())),
        UPTIME("Uptime", FlareBot.getInstance().getUptime()),
        MEM_USAGE("Memory Usage", getMb(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())),
        MEM_FREE("Memory Free", getMb(Runtime.getRuntime().freeMemory())),
        VIDEO_THREADS("Video Threads", String.valueOf(VideoThread.VIDEO_THREADS.activeCount())),
        TOTAL_THREADS("Total Threads", String.valueOf(Thread.getAllStackTraces().size())),
        VERSION("Version", FlareBot.getInstance().getVersion()),
        D4J_VERSION("Discord4J version", Discord4J.VERSION),
        GIT("Git Revision", (git != null ? git : "Unknown")),
        CPU_USAGE("CPU Usage", ((int) (ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getSystemCpuLoad() * 10000)) / 100f + "%"),
        SUPPORT_SERVER("Support Server", "[`Discord`](http://discord.me/flarebot)"),
        DONATIONS("Donate", "[`PayPal`](https://www.paypal.me/FlareBot/)"),
        PATREON("Our Patreon", "[`Patreon`](https://www.patreon.com/discordflarebot)"),
        WEBSITE("Website", "[`FlareBot`](http://flarebot.stream/)"),
        TWITTER("Twitter", "[`Twitter`](https://twitter.com/DiscordFlareBot)"),
        INVITE("Invite", String.format("[`Invite`](%s)", FlareBot.getInstance().getInvite())),
        EMPTY("\u200B", "\u200B", false),
        MADE_BY("Made By", "bwfcwalshy#1284 and Arsen#3291"),
        SOURCE("Source", "[`GitHub`](https://github.com/FlareBot/FlareBot)");

        private String name;
        private String returns;
        private boolean align = true;

        public static Content[] values = values();
        Content(String name, String returns) {
            this.name = name;
            this.returns = returns;
        }

        Content(String name, String returns, boolean align) {
            this.name = name;
            this.returns = returns;
            this.align = align;
        }

        public String getName() {
            return name;
        }

        public String getReturn() {
            return returns;
        }

        public boolean isAlign() {
            return this.align;
        }
    }
}
