package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import spark.utils.IOUtils;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.implementations.MultiSelectionContent;

import java.awt.Color;
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
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            EmbedBuilder bld = MessageUtils.getEmbed()
                    .setThumbnail(MessageUtils.getAvatar(channel.getJDA().getSelfUser()))
                    .setFooter("Made by Walshy#9060 and BinaryOverload#2382", channel.getJDA().getSelfUser().getEffectiveAvatarUrl());
            bld.setDescription("FlareBot v" + FlareBot.getInstance().getVersion() + " info")
                    .setColor(Color.CYAN);
            for (Content content : Content.values) {
                bld.addField(content.getName(), content.getReturn(), content.isAlign());
            }
            channel.sendMessage(bld.build()).queue();
        } else
            GeneralUtils.handleMultiSelectionCommand(sender, channel, args, Content.values);
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
    public String getUsage() {
        return "`{%}info [section]` - Sends info about the bot.";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    public enum Content implements MultiSelectionContent<String, String, Boolean> {

        SERVERS("Servers", () -> String.valueOf(FlareBot.getInstance().getGuilds().size())),
        VERSION("Version", FlareBot.getInstance().getVersion()),
        JDA_VERSION("JDA version", JDAInfo.VERSION),
        GIT("Git Revision", (git != null ? git : "Unknown")),
        SOURCE("Source", "[`GitHub`](https://github.com/FlareBot/FlareBot)"),
        INVITE("Invite", String.format("[`Invite`](%s)", FlareBot.getInstance().getInvite())),
        EMPTY("\u200B", "\u200B", false),
        SUPPORT_SERVER("Support Server", "[`Discord`](https://flarebot.stream/support-server)"),
        WEBSITE("Website", "[`FlareBot`](http://flarebot.stream/)"),
        PATREON("Our Patreon", "[`Patreon`](https://www.patreon.com/flarebot)"),
        DONATIONS("Donate", "[`PayPal`](https://www.paypal.me/walshydev/)"),
        TWITTER("Twitter", "[`Twitter`](https://twitter.com/DiscordFlareBot)"),
        TWITCH("Twitch", "[`Twitch`](https://twitch.tv/discordflarebot)");
        //EMPTY_1("\u200B", "\u200B", false),
        //MADE_BY("Originally Made By", "Walshy#9060 and Arsen#7525"),
        //DEVELOPERS("Current Developers", "Walshy#9060 and BinaryOverload#2382");

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

        public String getName() {
            return name;
        }

        public String getReturn() {
            return returns.get();
        }

        public Boolean isAlign() {
            return this.align;
        }
    }
}
