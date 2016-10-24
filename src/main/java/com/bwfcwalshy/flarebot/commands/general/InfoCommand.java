package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.sun.management.OperatingSystemMXBean;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.lang.management.ManagementFactory;

public class InfoCommand implements Command {

    private FlareBot flareBot;
    private IDiscordClient client;
    private Runtime runtime;

    public InfoCommand(FlareBot flareBot) {
        this.flareBot = flareBot;
        this.client = flareBot.getClient();
        this.runtime = Runtime.getRuntime();
    }

    private static final String DIVIDER = "+----------------------------------+";

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        String msg = "```FlareBot v" + FlareBot.getInstance().getVersion() + " Info"
                + "\n" + DIVIDER
                + "\nServers: " + client.getGuilds().size()
                + "\nVoice Channels: " + client.getConnectedVoiceChannels().size()
                + "\nText Channels: " + client.getChannels().size()
                + "\nUptime: " + flareBot.getUptime()
                + "\nMemory Usage: " + getMb(runtime.totalMemory() - runtime.freeMemory())
                + "\nFree Memory: " + getMb(runtime.freeMemory())
                + "\nDiscord4J Version: " + Discord4J.VERSION
                + "\nCPU Usage: " + ((int) (ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getSystemCpuLoad() * 10000)) / 100f
                + "%\n" + DIVIDER
                + "\nSupport Server: http://discord.me/flarebot"
                + "\nMade with love by bwfcwalshy#1284 and Arsen#3291\n"
                + "```\n"
                + "https://github.com/ArsenArsen/FlareBot";

        MessageUtils.sendMessage(channel, msg);
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
}
