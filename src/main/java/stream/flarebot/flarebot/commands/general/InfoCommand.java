package stream.flarebot.flarebot.commands.general;

import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.music.VideoThread;
import stream.flarebot.flarebot.util.CPUDaemon;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import spark.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

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
                FlareBot.LOGGER.error("Could not compare git revisions!", e1);
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
            StringBuilder search = new StringBuilder();
            for (String arg : args) {
                search.append(arg).append(" ");
            }
            search = new StringBuilder(search.toString().trim());

            if (search.toString().equalsIgnoreCase("here")) {
                EmbedBuilder bld = MessageUtils.getEmbed(sender).setThumbnail(MessageUtils.getAvatar(channel.getJDA().getSelfUser()));
                bld.setDescription("FlareBot v" + FlareBot.getInstance().getVersion() + " info");
                for (Content content : Content.values) {
                    bld.addField(content.getName(), content.getReturn(), content.isAlign());
                }
                channel.sendMessage(bld.build()).queue();
                return;
            }

            for (Content content : Content.values) {
                if (search.toString().equalsIgnoreCase(content.getName()) || search.toString().replaceAll("_", " ").equalsIgnoreCase(content.getName())) {
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
        SERVERS("Servers", () -> FlareBot.getInstance().getGuilds().size()),
        TOTAL_USERS("Total Users", () -> FlareBot.getInstance().getUsers().size()),
        VOICE_CONNECTIONS("Voice Connections", () -> FlareBot.getInstance().getConnectedVoiceChannels().size()),
        ACTIVE_CHANNELS("Channels Playing Music", () -> FlareBot.getInstance().getActiveVoiceChannels()),
        TEXT_CHANNELS("Text Channels", () -> FlareBot.getInstance().getChannels().size()),
        UPTIME("Uptime", () -> FlareBot.getInstance().getUptime()),
        MEM_USAGE("Memory Usage", () -> getMb(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())),
        MEM_FREE("Memory Free", () -> getMb(Runtime.getRuntime().freeMemory())),
        VIDEO_THREADS("Video Threads", VideoThread.VIDEO_THREADS::activeCount),
        TOTAL_THREADS("Total Threads", () -> Thread.getAllStackTraces().size()),
        VERSION("Version", FlareBot.getInstance().getVersion()),
        JDA_VERSION("JDA version", JDAInfo.VERSION),
        GIT("Git Revision", (git != null ? git : "Unknown")),
        CPU_USAGE("CPU Usage", () -> ((int) (CPUDaemon.get() * 10000)) / 100d + "%"),
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
        private Supplier<Object> returns;
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

        Content(String name, Supplier<Object> returns) {
            this.name = name;
            this.returns = returns;
        }

        Content(String name, Supplier<Object> returns, boolean align) {
            this.name = name;
            this.returns = returns;
            this.align = align;
        }

        public String getName() {
            return name;
        }

        public String getReturn() {
            return String.valueOf(returns.get());
        }

        public boolean isAlign() {
            return this.align;
        }
    }
}
