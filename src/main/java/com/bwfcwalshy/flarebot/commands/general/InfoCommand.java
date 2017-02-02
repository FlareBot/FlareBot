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

import static com.bwfcwalshy.flarebot.FlareBot.LOGGER;
import static com.bwfcwalshy.flarebot.FlareBot.getInstance;

public class InfoCommand implements Command {

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
                    git = IOUtils.toString(pr.getInputStream());
                }
            } catch (InterruptedException | IOException e1) {
                LOGGER.error("Could not compare git revisions!", e1);
            }
        }
    }

    public InfoCommand() {
        this.runtime = Runtime.getRuntime();
    }


    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        EmbedBuilder bld = MessageUtils.getEmbed(sender).setThumbnail(MessageUtils.getAvatar(sender.getJDA().getSelfUser()));
        bld.setDescription("FlareBot v" + FlareBot.getInstance().getVersion() + " info");
        bld.addField("Servers: ", String.valueOf(FlareBot.getInstance().getGuilds().size()), true);
        bld.addField("Voice Connections: ", String.valueOf(getInstance().getConnectedVoiceChannels().size()), true);
        bld.addField("Channels playing music: ", String.valueOf(FlareBot.getInstance().getConnectedVoiceChannels().stream()
                .map(VoiceChannel::getGuild)
                .map(ISnowflake::getId)
                .filter(gid -> FlareBot.getInstance().getMusicManager().hasPlayer(gid))
                .map(g -> FlareBot.getInstance().getMusicManager().getPlayer(g))
                .filter(p -> p.getPlayingTrack() != null)
                .filter(p -> !p.getPaused()).count()), true);
        bld.addField("Text Channels: ", String.valueOf(FlareBot.getInstance().getChannels().size()), true);
        bld.addField("Uptime: ", FlareBot.getInstance().getUptime(), true);
        bld.addField("Memory Usage: ", getMb(runtime.totalMemory() - runtime.freeMemory()), true);
        bld.addField("Memory Free: ", getMb(runtime.freeMemory()), true);
        bld.addField("Video threads: ", String.valueOf(VideoThread.VIDEO_THREADS.activeCount()), true);
        bld.addField("Total threads: ", String.valueOf(Thread.getAllStackTraces().size()), true);
        bld.addField("JDA Version: ", JDAInfo.VERSION, true);
        if (git != null)
            bld.addField("Git revision: ", git, true);
        bld.addField("CPU Usage: ",
                ((int) (ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getSystemCpuLoad() * 10000)) / 100f + "%", true);
        bld.addField("Support Server: ", "[`Discord`](http://discord.me/flarebot)", true);
        bld.addField("Donate to our host: ", "[`PayPal`](https://www.paypal.me/FlareBot/)", true);
        bld.addField("Our Patreon: ", "[`Patreon`](https://www.patreon.com/discordflarebot)", true);
        bld.addField("Website: ", "[`FlareBot`](http://flarebot.stream/)", true);
        bld.addField("Twitter: ", "[`Twitter`](https://twitter.com/DiscordFlareBot)", true);
        bld.addField("Invite: ", String.format("[`Invite`](%s)", FlareBot.getInstance().getInvite()), true);
        bld.addField("\u200B", "\u200B", false);
//        bld.addField("\u200B", "\u200B", true);
        bld.addField("Made By: ", "bwfcwalshy#1284 and Arsen#3291", true);
        bld.addField("Source: ", "[`GitHub`](https://github.com/FlareBot/FlareBot)", true);

        MessageUtils.sendMessage(bld, channel);
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
