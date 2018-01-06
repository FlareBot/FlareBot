package stream.flarebot.flarebot.util;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Getters;
import stream.flarebot.flarebot.commands.Command;

import java.time.Clock;
import java.time.LocalDateTime;

public class Constants {

    public static final String OFFICIAL_GUILD = "226785954537406464";
    public static final String INVITE_URL = "https://discord.gg/TTAUGvZ";
    public static final String FLAREBOT_API = "https://api.flarebot.stream/";

    public static final long DEVELOPER_ID = 226788297156853771L;
    public static final long CONTRIBUTOR_ID = 272324832279003136L;
    public static final long STAFF_ID = 320327762881675264L;

    private static final String FLARE_TEST_BOT_CHANNEL = "242297848123621376";
    public static final char COMMAND_CHAR = '_';

    public static Guild getOfficialGuild() {
        return Getters.getGuildById(OFFICIAL_GUILD);
    }

    public static TextChannel getErrorLogChannel() {
        return (FlareBot.instance.isTestBot() ?
                Getters.getChannelById(Constants.FLARE_TEST_BOT_CHANNEL) :
                Getters.getChannelById("226786557862871040"));
    }

    public static TextChannel getGuildLogChannel() {
        return (FlareBot.instance.isTestBot() ?
                Getters.getChannelById(Constants.FLARE_TEST_BOT_CHANNEL) :
                Getters.getChannelById("260401007685664768"));
    }

    private static TextChannel getEGLogChannel() {
        return (FlareBot.instance.isTestBot() ?
                Getters.getChannelById(Constants.FLARE_TEST_BOT_CHANNEL) :
                Getters.getChannelById("358950369642151937"));
    }

    public static void logEG(String eg, Command command, Guild guild, User user) {
        EmbedBuilder builder = new EmbedBuilder().setTitle("Found `" + eg + "`")
                .addField("Guild", guild.getId() + " (`" + guild.getName() + "`) ", true)
                .addField("User", user.getAsMention() + " (`" + user.getName() + "#" + user.getDiscriminator() + "`)", true)
                .setTimestamp(LocalDateTime.now(Clock.systemUTC()));
        if (command != null) builder.addField("Command", command.getCommand(), true);
        Constants.getEGLogChannel().sendMessage(builder.build()).queue();
    }

    public static TextChannel getImportantLogChannel() {
        return (FlareBot.instance.isTestBot() ?
                Getters.getChannelById(Constants.FLARE_TEST_BOT_CHANNEL) :
                Getters.getChannelById("358978253966278657"));
    }

}
