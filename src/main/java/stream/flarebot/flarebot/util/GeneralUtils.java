package stream.flarebot.flarebot.util;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Markers;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.Report;
import stream.flarebot.flarebot.objects.ReportMessage;

import javax.net.ssl.HttpsURLConnection;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class GeneralUtils {

    private static final DecimalFormat percentageFormat = new DecimalFormat("#.##");
    private static final Pattern userDiscrim = Pattern.compile(".+#[0-9]{4}");

    public static String getShardId(JDA jda) {
        return jda.getShardInfo() == null ? "1" : String.valueOf(jda.getShardInfo().getShardId() + 1);
    }

    public static int getShardIdAsInt(JDA jda) {
        return jda.getShardInfo() == null ? 1 : jda.getShardInfo().getShardId() + 1;
    }

    public static EmbedBuilder getReportEmbed(User sender, Report report) {
        EmbedBuilder eb = MessageUtils.getEmbed(sender);
        User reporter = FlareBot.getInstance().getUserByID(String.valueOf(report.getReporterId()));
        User reported = FlareBot.getInstance().getUserByID(String.valueOf(report.getReportedId()));

        eb.addField("Report ID", String.valueOf(report.getId()), true);
        eb.addField("Reporter", MessageUtils.getTag(reporter), true);
        eb.addField("Reported", MessageUtils.getTag(reported), true);

        eb.addField("Time", report.getTime().toLocalDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " GMT/BST", true);
        eb.addField("Status", report.getStatus().getMessage(), true);

        eb.addField("Message", "```" + report.getMessage() + "```", false);
        StringBuilder builder = new StringBuilder("The last 5 messages by the reported user: ```\n");
        for (ReportMessage m : report.getMessages()) {
            builder.append("[" + m.getTime().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " GMT/BST] ")
                    .append(GeneralUtils.truncate(100, m.getMessage()))
                    .append("\n");
        }
        builder.append("```");
        eb.addField("Messages from reported user", builder.toString(), false);
        return eb;
    }

    public static String getPageOutOfTotal(int page, List<?> items, int pageLength) {
        return String.valueOf(page) + "/" + String.valueOf(items.size() < pageLength ? 1 : (items.size() / pageLength) + (items.size() % pageLength != 0 ? 1 : 0));
    }

    public static String formatDuration(long duration) {
        long totalSeconds = duration / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = (totalSeconds / 3600);
        return (hours > 0 ? (hours < 10 ? "0" + hours : hours) + ":" : "")
                + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    public static String getProgressBar(Track track) {
        float percentage = (100f / track.getTrack().getDuration() * track.getTrack().getPosition());
        return "[" + StringUtils.repeat("▬", (int) Math.round((double) percentage / 10)) +
                "]()" +
                StringUtils.repeat("▬", 10 - (int) Math.round((double) percentage / 10)) +
                " " + GeneralUtils.percentageFormat.format(percentage) + "%";
    }

    private static char getPrefix(TextChannel channel) {
        if (channel.getGuild() != null) {
            return FlareBot.getPrefixes().get(channel.getGuild().getId());
        }
        return FlareBot.getPrefixes().get(null);
    }

    public static String formatCommandPrefix(TextChannel channel, String usage) {
        String prefix = String.valueOf(getPrefix(channel));
        return usage.replaceAll("\\{%}", prefix);
    }

    public static AudioItem resolveItem(Player player, String input) throws IllegalArgumentException, IllegalStateException {
        Optional<AudioItem> item = Optional.empty();
        boolean failed = false;
        int backoff = 2;
        Throwable cause = null;
        for (int i = 0; i <= 2; i++) {
            try {
                item = Optional.ofNullable(player.resolve(input));
                failed = false;
                break;
            } catch (FriendlyException | InterruptedException | ExecutionException e) {
                failed = true;
                cause = e;
                if (e.getMessage().contains("Vevo")) {
                    throw new IllegalStateException(Jsoup.clean(cause.getMessage(), Whitelist.none()), cause);
                }
                FlareBot.LOGGER.error(Markers.NO_ANNOUNCE, "Cannot get video '" + input + "'");
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ignored) {
                }
                backoff ^= 2;
            }
        }
        if (failed) {
            throw new IllegalStateException(Jsoup.clean(cause.getMessage(), Whitelist.none()), cause);
        } else if (!item.isPresent()) {
            throw new IllegalArgumentException();
        }
        return item.get();
    }

    public static int getGuildUserCount(Guild guild) {
        int i = 0;
        for (Member member : guild.getMembers()) {
            if (!member.getUser().isBot()) {
                i++;
            }
        }
        return i;
    }

    public static String colourFormat(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static String truncate(int length, String string) {
        return truncate(length, string, true);
    }

    public static String truncate(int length, String string, boolean ellipse) {
        return string.substring(0, Math.min(string.length(), length)) + (string.length() > length ? "..." : "");
    }

    public static List<Role> getRole(String string, Guild guild) {
        return guild.getRolesByName(string, true);
    }

    public static User getUser(String s) {
        return getUser(s, null);
    }

    public static User getUser(String s, String guildId) {
        return getUser(s, guildId, false);
    }

    public static User getUser(String s, boolean forceGet) {
        return getUser(s, null, forceGet);
    }

    public static User getUser(String s, String guildId, boolean forceGet) {
        if (userDiscrim.matcher(s).find()) {
            if (guildId == null || guildId.isEmpty()) {
                return FlareBot.getInstance().getUsers().stream()
                        .filter(user -> (user.getName() + "#" + user.getDiscriminator()).equalsIgnoreCase(s))
                        .findFirst().orElse(null);
            } else {
                try {
                    return FlareBot.getInstance().getGuildByID(guildId).getMembers().stream()
                            .map(Member::getUser)
                            .filter(user -> (user.getName() + "#" + user.getDiscriminator()).equalsIgnoreCase(s))
                            .findFirst().orElse(null);
                } catch (NullPointerException ignored) {
                }
            }
        } else {
            User tmp;
            if (guildId == null || guildId.isEmpty()) {
                tmp = FlareBot.getInstance().getUsers().stream().filter(user -> user.getName().equalsIgnoreCase(s))
                        .findFirst().orElse(null);
            } else {
                tmp = FlareBot.getInstance().getGuildByID(guildId).getMembers().stream()
                        .map(Member::getUser)
                        .filter(user -> user.getName().equalsIgnoreCase(s))
                        .findFirst().orElse(null);
            }
            if (tmp != null) return tmp;
            try {
                long l = Long.parseLong(s.replaceAll("[^0-9]", ""));
                if (guildId == null || guildId.isEmpty()) {
                    tmp = FlareBot.getInstance().getUserById(l);
                } else {
                    Member temMember = FlareBot.getInstance().getGuildByID(guildId).getMemberById(l);
                    if (temMember != null) {
                        tmp = temMember.getUser();
                    }
                }
                if (tmp != null) {
                    return tmp;
                } else if (forceGet) {
                    return FlareBot.getInstance().retrieveUserById(l);
                }
            } catch (NumberFormatException | NullPointerException ignored) {
            }
        }
        return null;
    }

    public static Role getRole(String s, String guildId) {
        Role role = FlareBot.getInstance().getGuildByID(guildId).getRoles().stream()
                .filter(r -> r.getName().equalsIgnoreCase(s))
                .findFirst().orElse(null);
        if (role != null) return role;
        try {
            role = FlareBot.getInstance().getGuildByID(guildId).getRoleById(Long.parseLong(s.replaceAll("[^0-9]", "")));
            if (role != null) return role;
        } catch (NumberFormatException | NullPointerException ignored) {
        }
        return null;
    }

    public static boolean validPerm(String perm) {
        if (perm.equals("*") || perm.equals("flarebot.*")) return true;
        if (perm.startsWith("flarebot.") && perm.split("\\.").length >= 2) {
            perm = perm.substring(perm.indexOf(".") + 1);
            String command = perm.split("\\.")[0];
            for (Command c : FlareBot.getInstance().getCommands()) {
                if (c.getCommand().equalsIgnoreCase(command) && c.getType() != CommandType.SECRET) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void joinChannel(TextChannel channel, Member member) {
        if (channel.getGuild().getSelfMember()
                .hasPermission(member.getVoiceState().getChannel(), Permission.VOICE_CONNECT) &&
                channel.getGuild().getSelfMember()
                        .hasPermission(member.getVoiceState().getChannel(), Permission.VOICE_SPEAK)) {
            if (member.getVoiceState().getChannel().getUserLimit() > 0 && member.getVoiceState().getChannel()
                    .getMembers().size()
                    >= member.getVoiceState().getChannel().getUserLimit() && !member.getGuild().getSelfMember()
                    .hasPermission(member
                            .getVoiceState()
                            .getChannel(), Permission.MANAGE_CHANNEL)) {
                MessageUtils.sendErrorMessage("We can't join :(\n\nThe channel user limit has been reached and we don't have the 'Manage Channel' permission to " +
                        "bypass it!", channel);
                return;
            }
            channel.getGuild().getAudioManager().openAudioConnection(member.getVoiceState().getChannel());
        } else {
            MessageUtils.sendErrorMessage("I do not have permission to " + (!channel.getGuild().getSelfMember()
                    .hasPermission(member.getVoiceState()
                            .getChannel(), Permission.VOICE_CONNECT) ?
                    "connect" : "speak") + " in your voice channel!", channel);
        }
    }

    public static <T extends Comparable> List<T> orderList(Collection<? extends T> strings) {
        List<T> list = new ArrayList<>(strings);
        Collections.sort(list, Comparable::compareTo);
        return list;
    }

    public static Emote getEmoteById(long l) {
        return FlareBot.getInstance().getGuilds().stream().map(g -> g.getEmoteById(l))
                .filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * This will download and cache the image if not found already!
     *
     * @param fileUrl  Url to download the image from.
     * @param fileName Name of the image file.
     * @param user     User to send the image to.
     */
    public static void sendImage(String fileUrl, String fileName, User user) {
        try {
            File dir = new File("imgs");
            if (!dir.exists())
                dir.mkdir();
            File trap = new File("imgs" + File.separator + fileName);
            if (!trap.exists()) {
                trap.createNewFile();
                URL url = new URL(fileUrl);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 FlareBot");
                InputStream is = conn.getInputStream();
                OutputStream os = new FileOutputStream(trap);
                byte[] b = new byte[2048];
                int length;
                while ((length = is.read(b)) != -1) {
                    os.write(b, 0, length);
                }
                is.close();
                os.close();
            }
            user.openPrivateChannel().complete().sendFile(trap, fileName, null)
                    .queue();
        } catch (IOException | ErrorResponseException e) {
            FlareBot.LOGGER.error("Unable to send image", e);
        }
        return;
    }

    public static boolean canChangeNick(String guildId) {
        if (FlareBot.getInstance().getGuildByID(guildId) != null) {
            return FlareBot.getInstance().getGuildByID(guildId).getSelfMember().hasPermission(Permission.NICKNAME_CHANGE) ||
                    FlareBot.getInstance().getGuildByID(guildId).getSelfMember().hasPermission(Permission.NICKNAME_MANAGE);
        } else
            return false;
    }

    public static String getStackTrace(Throwable e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        printWriter.close();
        return writer.toString();
    }
    
    public static int getInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
