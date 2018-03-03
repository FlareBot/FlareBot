package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class ServerInfoCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            sendGuildInfo(guild.getGuild(), channel);
        } else {
            Guild targetGuild = FlareBot.getInstance().getGuildById(GeneralUtils.getLong(args[0], -1));
            if (targetGuild != null) {
                sendGuildInfo(targetGuild, channel);
            } else {
                MessageUtils.sendErrorMessage("We couldn't find that guild.", channel);
            }
        }
    }

    private void sendGuildInfo(Guild guild, TextChannel channel) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(guild.getName());
        eb.setThumbnail(guild.getIconUrl());
        eb.addField("Users", "**Total:** " +
                guild.getMembers().size() + "\n" +
                "\n" +
                "**Online:** " +
                guild.getMembers().stream().filter(member -> !member.getOnlineStatus().equals(OnlineStatus.OFFLINE)).count() + "\n" +
                "\n" +
                "**Owner:** " +
                MessageUtils.getTag(guild.getOwner().getUser()), true);
        String afk = guild.getAfkChannel() == null ? "" :
                "**AFK:**\n" +
                        "Channel: " +
                        guild.getAfkChannel().getName() + "\n" +
                        "\n" +
                        "Timeout: " +
                        FlareBot.getInstance().formatTime(guild.getAfkTimeout().getSeconds(), TimeUnit.SECONDS, true, false);
        eb.addField("Channels", "**Text**\n" +
                "Total: " +
                guild.getTextChannels().size() + "\n" +
                "\n" +
                "**Voice**\n" +
                "Total: " +
                guild.getVoiceChannels().size() + "\n" +
                "\n" +
                "Active: " +
                guild.getVoiceChannels().stream().filter(voiceChannel -> voiceChannel.getMembers().size() > 0).count() + "\n" +
                "\n" + afk, true);
        eb.addField("Misc info", "**Creation time:** " +
                guild.getCreationTime().toLocalDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " UTC\n" +
                "\n" +
                "**Roles:** " +
                guild.getRoles().size() + "\n" +
                "\n" +
                "**Server region:** " +
                guild.getRegion().getName() + "\n" +
                "\n" +
                "**Verification Level:** " +
                GeneralUtils.getVerificationString(guild.getVerificationLevel()), true);
        eb.setFooter("ID: " + guild.getId(), null);
        eb.setColor(Color.CYAN);
        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public String getCommand() {
        return "serverinfo";
    }

    @Override
    public String getDescription() {
        return "Get's info on a guild (server).";
    }

    @Override
    public String getUsage() {
        return "`{%}serverinfo [guild_id]` - Gets the info on a guild (server).";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"guildinfo"};
    }
}
