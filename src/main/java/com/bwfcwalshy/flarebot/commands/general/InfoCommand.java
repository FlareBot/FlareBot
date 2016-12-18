package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.music.VideoThread;
import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.io.IOUtils;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;

import static com.bwfcwalshy.flarebot.FlareBot.LOGGER;

public class InfoCommand implements Command {

    private IDiscordClient client;
    private Runtime runtime;
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

    public InfoCommand(FlareBot flareBot) {
        this.client = flareBot.getClient();
        this.runtime = Runtime.getRuntime();
    }


    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        EmbedBuilder bld = MessageUtils.getEmbed(sender).withThumbnail(MessageUtils.getAvatar(channel.getClient().getOurUser()));
        bld.withDesc("FlareBot v" + FlareBot.getInstance().getVersion() + " info");
        bld.appendField("Servers: ", String.valueOf(client.getGuilds().size()), true);
        bld.appendField("Voice Connections: ", String.valueOf(client.getConnectedVoiceChannels().size()), true);
        bld.appendField("Channels playing music: ", String.valueOf(String.valueOf(client.getGuilds().stream().map(IDiscordObject::getID)
                .filter(gid -> FlareBot.getInstance().getMusicManager().hasPlayer(gid))
                .map(g -> FlareBot.getInstance().getMusicManager().getPlayer(g))
                .filter(p -> p.getPlayingTrack() != null)
                .filter(p -> !p.getPaused()).count())), true);
        bld.appendField("Text Channels: ", String.valueOf(client.getChannels(false).size()), true);
        bld.appendField("Uptime: ", FlareBot.getInstance().getUptime(), true);
        bld.appendField("Memory Usage: ", getMb(runtime.totalMemory() - runtime.freeMemory()), true);
        bld.appendField("Memory Free: ", getMb(runtime.freeMemory()), true);
        bld.appendField("Video download and search threads: ", String.valueOf(VideoThread.VIDEO_THREADS.activeCount()), true);
        bld.appendField("Total threads: ", String.valueOf(Thread.getAllStackTraces().size()), true);
        bld.appendField("Discord4J Version: ", Discord4J.VERSION, true);
        if (git != null)
            bld.appendField("Git revision: ", git, true);
        bld.appendField("CPU Usage: ",
                ((int) (ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getSystemCpuLoad() * 10000)) / 100f + "%", true);
        bld.appendField("Support Server: ", "[`Discord`](http://discord.me/flarebot)", true);
        bld.appendField("Donate to our host: ", "[`PayPal`](https://www.paypal.me/CaptainBaconz)", true);
        bld.appendField("Invite: ", String.format("[`Invite`](%s)", FlareBot.getInstance().getInvite()), true);
        bld.appendField("\u200B", "\u200B", false);
//        bld.appendField("\u200B", "\u200B", true);
        bld.appendField("Made By: ", "bwfcwalshy#1284 and Arsen#3291", true);
        bld.appendField("Source: ", "[`GitHub`](https://github.com/ArsenArsen/FlareBot)", true);

        MessageUtils.sendMessage(bld.build(), channel);
    }

    private String getMb(long bytes) {
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

    public static String gitGit() {
        return git;
    }
}
