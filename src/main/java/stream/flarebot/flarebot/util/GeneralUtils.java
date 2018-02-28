package stream.flarebot.flarebot.util;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.google.gson.JsonElement;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import io.github.binaryoverload.JSONConfig;
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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.database.RedisMessage;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.Report;
import stream.flarebot.flarebot.objects.ReportMessage;
import stream.flarebot.flarebot.util.errorhandling.Markers;
import stream.flarebot.flarebot.util.implementations.MultiSelectionContent;

public class GeneralUtils {

    private static final DecimalFormat percentageFormat = new DecimalFormat("#.##");
    private static final Pattern userDiscrim = Pattern.compile(".+#[0-9]{4}");
    private static final Pattern timeRegex = Pattern.compile("^([0-9]*):?([0-9]*)?:?([0-9]*)?$");

    private static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("MMMM yyyy HH:mm:ss");

    private static final SimpleDateFormat preciseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");

    private static final PeriodFormatter prettyTime = new PeriodFormatterBuilder()
            .appendDays().appendSuffix(" Day ", " Days ")
            .appendHours().appendSuffix(" Hour ", " Hours ")
            .appendMinutes().appendSuffix(" Minute ", " Minutes ")
            .appendSeconds().appendSuffix(" Second", " Seconds")
            .toFormatter();
    private static final PeriodFormatter periodParser = new PeriodFormatterBuilder()
            .appendDays().appendSuffix("d")
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .appendSeconds().appendSuffix("s")
            .toFormatter();
    private static final int LEVENSHTEIN_DISTANCE = 8;

    public static EmbedBuilder getReportEmbed(User sender, Report report) {
        EmbedBuilder eb = MessageUtils.getEmbed(sender);
        User reporter = FlareBot.getInstance().getUserById(String.valueOf(report.getReporterId()));
        User reported = FlareBot.getInstance().getUserById(String.valueOf(report.getReportedId()));

        eb.addField("Report ID", String.valueOf(report.getId()), true);
        eb.addField("Reporter", MessageUtils.getTag(reporter), true);
        eb.addField("Reported", MessageUtils.getTag(reported), true);

        eb.addField("Time", report.getTime().toLocalDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " GMT/BST", true);
        eb.addField("Status", report.getStatus().getMessage(), true);

        eb.addField("Message", "```" + report.getMessage() + "```", false);
        StringBuilder builder = new StringBuilder("The last 5 messages by the reported user: ```\n");
        for (ReportMessage m : report.getMessages()) {
            builder.append("[").append(m.getTime().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append(" GMT/BST] ")
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
                "](https://github.com/FlareBot)" +
                StringUtils.repeat("▬", 10 - (int) Math.round((double) percentage / 10)) +
                " " + GeneralUtils.percentageFormat.format(percentage) + "%";
    }

    private static char getPrefix(TextChannel channel) {
        if (channel != null) {
            return FlareBot.getPrefixes().get(channel.getGuild().getId());
        }
        return FlareBot.getPrefixes().get(null);
    }

    public static String formatCommandPrefix(TextChannel channel, String usage) {
        String prefix = String.valueOf(getPrefix(channel));
        if (usage.contains("{%}"))
            return usage.replaceAll("\\{%}", prefix);
        return usage;
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
        return string.substring(0, Math.min(string.length(), length - (ellipse ? 3 : 0))) + (string.length() >
                length - (ellipse ? 3 : 0) && ellipse ? "..." : "");
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
                    return FlareBot.getInstance().getGuildById(guildId).getMembers().stream()
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
                if (FlareBot.getInstance().getGuildById(guildId) != null) {
                    tmp = FlareBot.getInstance().getGuildById(guildId).getMembers().stream()
                            .map(Member::getUser)
                            .filter(user -> user.getName().equalsIgnoreCase(s))
                            .findFirst().orElse(null);
                } else
                    tmp = null;
            }
            if (tmp != null) return tmp;
            try {
                long l = Long.parseLong(s.replaceAll("[^0-9]", ""));
                if (guildId == null || guildId.isEmpty()) {
                    tmp = FlareBot.getInstance().getUserById(l);
                } else {
                    Member temMember = FlareBot.getInstance().getGuildById(guildId).getMemberById(l);
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
        return getRole(s, guildId, null);
    }

    public static Role getRole(String s, String guildId, TextChannel channel) {
        Guild guild = FlareBot.getInstance().getGuildById(guildId);
        Role role = guild.getRoles().stream()
                .filter(r -> r.getName().equalsIgnoreCase(s))
                .findFirst().orElse(null);
        if (role != null) return role;
        try {
            role = guild.getRoleById(Long.parseLong(s.replaceAll("[^0-9]+", "")));
            if (role != null) return role;
        } catch (NumberFormatException | NullPointerException ignored) {
        }
        if (channel != null) {
            if (guild.getRolesByName(s, true).isEmpty()) {
                String closest = null;
                int distance = LEVENSHTEIN_DISTANCE;
                for (Role role1 : guild.getRoles().stream().filter(role1 -> FlareBotManager.getInstance().getGuild(guildId).getSelfAssignRoles()
                        .contains(role1.getId())).collect(Collectors.toList())) {
                    int currentDistance = StringUtils.getLevenshteinDistance(role1.getName(), s);
                    if (currentDistance < distance) {
                        distance = currentDistance;
                        closest = role1.getName();
                    }
                }
                MessageUtils.sendErrorMessage("That role does not exist! "
                        + (closest != null ? "Maybe you mean `" + closest + "`" : ""), channel);
                return null;
            } else {
                return guild.getRolesByName(s, true).get(0);
            }
        }
        return null;
    }

    public static TextChannel getChannel(String arg) {
        return getChannel(arg, null);
    }

    public static TextChannel getChannel(String channelArg, GuildWrapper wrapper) {
        try {
            long channelId = Long.parseLong(channelArg.replaceAll("[^0-9]", ""));
            return wrapper != null ? wrapper.getGuild().getTextChannelById(channelId) : FlareBot.getInstance().getChannelById(channelId);
        } catch (NumberFormatException e) {
            if (wrapper != null) {
                List<TextChannel> tcs = wrapper.getGuild().getTextChannelsByName(channelArg, true);
                if (!tcs.isEmpty()) {
                    return tcs.get(0);
                }
            }
            return null;
        }
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
        GuildVoiceState vs = channel.getGuild().getSelfMember().getVoiceState();
        GuildVoiceState memberVS = member.getVoiceState();
        if (memberVS.getChannel() == null) {
            MessageUtils.sendErrorMessage("You need to join a voice channel to do that!", channel);
            return; // They aren't in a VC so we can't join that.
        }
        if (vs.getChannel() != null && vs.getChannel().getIdLong() == member.getVoiceState().getChannel().getIdLong())
            return; // Already in the same VC as the user.

        if (channel.getGuild().getSelfMember()
                .hasPermission(memberVS.getChannel(), Permission.VOICE_CONNECT) &&
                channel.getGuild().getSelfMember()
                        .hasPermission(memberVS.getChannel(), Permission.VOICE_SPEAK)) {
            if (memberVS.getChannel().getUserLimit() > 0 && member.getVoiceState().getChannel()
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
            if (FlareBot.getInstance().getMusicManager().getPlayer(channel.getGuild().getId()).getPlaylist().isEmpty())
                FlareBotManager.getInstance().getLastActive().put(channel.getGuild().getIdLong(), System.currentTimeMillis());
        } else {
            MessageUtils.sendErrorMessage("I do not have permission to " + (!channel.getGuild().getSelfMember()
                    .hasPermission(member.getVoiceState()
                            .getChannel(), Permission.VOICE_CONNECT) ?
                    "connect" : "speak") + " in your voice channel!", channel);
        }
    }

    public static <T extends Comparable> List<T> orderList(Collection<? extends T> strings) {
        List<T> list = new ArrayList<>(strings);
        list.sort(Comparable::compareTo);
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
            if (!dir.exists() && !dir.mkdir())
                throw new IllegalStateException("Cannot create 'imgs' folder!");
            File image = new File("imgs" + File.separator + fileName);
            if (!image.exists() && image.createNewFile()) {
                URL url = new URL(fileUrl);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 FlareBot");
                InputStream is = conn.getInputStream();
                OutputStream os = new FileOutputStream(image);
                byte[] b = new byte[2048];
                int length;
                while ((length = is.read(b)) != -1) {
                    os.write(b, 0, length);
                }
                is.close();
                os.close();
            }
            user.openPrivateChannel().complete().sendFile(image, fileName, null)
                    .queue();
        } catch (IOException | ErrorResponseException e) {
            FlareBot.LOGGER.error("Unable to send image '" + fileName + "'", e);
        }
    }

    public static boolean canChangeNick(String guildId) {
        return FlareBot.getInstance().getGuildById(guildId) != null &&
                (FlareBot.getInstance().getGuildById(guildId).getSelfMember().hasPermission(Permission.NICKNAME_CHANGE) ||
                        FlareBot.getInstance().getGuildById(guildId).getSelfMember().hasPermission(Permission.NICKNAME_MANAGE));
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

    public static long getLong(String s, long defaultValue) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void methodErrorHandler(Logger logger, String startMessage,
                                          String successMessage, String errorMessage,
                                          Runnable runnable) {
        Objects.requireNonNull(successMessage);
        Objects.requireNonNull(errorMessage);
        if (startMessage != null) logger.info(startMessage);
        try {
            runnable.run();
            logger.info(successMessage);
        } catch (Exception e) {
            logger.error(errorMessage, e);
        }
    }

    /**
     * Get a Joda Period from the input string. This will convert something like `1d20s` to 1 day and 20 seconds in the
     * Joda Period.
     *
     * @param input The input string to parse.
     * @return The joda Period or null if the format is not correct.
     */
    public static Period getTimeFromInput(String input, TextChannel channel) {
        try {
            return periodParser.parsePeriod(input);
        } catch (IllegalArgumentException e) {
            MessageUtils.sendErrorMessage("The duration is not in the correct format! Try something like `1d`",
                    channel);
            return null;
        }
    }

    public static String formatJodaTime(Period period) {
        return period.toString(prettyTime).trim();
    }

    /**
     * This will format a Joda Period into a precise timestamp (yyyy-MM-dd HH:mm:ss.SS).
     *
     * @param period Period to format onto the current date
     * @return The date in a precise format. Example: 2017-10-13 21:56:33.681
     */
    public static String formatPrecisely(Period period) {
        return preciseFormat.format(DateTime.now(DateTimeZone.UTC).plus(period).toDate());
    }

    public static String formatTime(LocalDateTime dateTime) {
        LocalDateTime time = LocalDateTime.from(dateTime.atOffset(ZoneOffset.UTC));
        return time.getDayOfMonth() + getDayOfMonthSuffix(time.getDayOfMonth()) + " " + time
                .format(timeFormat) + " UTC";
    }

    private static String getDayOfMonthSuffix(final int n) {
        if (n < 1 || n > 31) throw new IllegalArgumentException("illegal day of month: " + n);
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public static String embedToText(MessageEmbed embed) {
        StringBuilder sb = new StringBuilder();

        if (embed.getTitle() != null)
            sb.append("**").append(embed.getTitle()).append("**: ");
        if (embed.getDescription() != null)
            sb.append(embed.getDescription()).append(" ");
        for (MessageEmbed.Field field : embed.getFields()) {
            sb.append("**").append(field.getName()).append("**: ").append(field.getValue()).append(" ");
        }
        if (embed.getFooter() != null)
            sb.append("*").append(embed.getFooter().getText()).append("*");
        return sb.toString();
    }

    public static Map<Boolean, List<Permission>> getChangedPerms(List<Permission> oldPerms, List<Permission> newPerms) {
        Map<Boolean, List<Permission>> changes = new HashMap<>();
        List<Permission> removed = new ArrayList<>();
        List<Permission> added = new ArrayList<>();
        for (Permission oldPerm : oldPerms) {
            if (!newPerms.contains(oldPerm)) {
                removed.add(oldPerm);
            }
        }
        for (Permission newPerm : newPerms) {
            if (!oldPerms.contains(newPerm)) {
                added.add(newPerm);
            }
        }
        changes.put(true, added);
        changes.put(false, removed);
        return changes;
    }

    /**
     * This is to handle "multi-selection commands" for example the info and stats commands which take one or more
     * arguments and get select data from an enum
     */
    public static void handleMultiSelectionCommand(User sender, TextChannel channel, String[] args,
                                                   MultiSelectionContent<String, String, Boolean>[] providedContent) {
        String search = FlareBot.getMessage(args);
        String[] fields = search.split(",");
        EmbedBuilder builder = MessageUtils.getEmbed(sender).setColor(Color.CYAN);
        boolean valid = false;
        for (String string : fields) {
            String s = string.trim();
            for (MultiSelectionContent<String, String, Boolean> content : providedContent) {
                if (s.equalsIgnoreCase(content.getName()) || s.replaceAll("_", " ")
                        .equalsIgnoreCase(content.getName())) {
                    builder.addField(content.getName(), content.getReturn(), content.isAlign());
                    valid = true;
                }
            }
        }
        if (valid) channel.sendMessage(builder.build()).queue();

        else MessageUtils.sendErrorMessage("That piece of information could not be found!", channel);
    }

    /**
     * Checks if paths exist in the given json
     * <p>
     * Key of the {@link Pair} is a list of the paths that exist in the JSON
     * Value of the {@link Pair} is a list of the paths that don't exist in the JSON
     *
     * @param json  The JSON to check <b>Mustn't be null</b>
     * @param paths The paths to check <b>Mustn't be null or empty</b>
     * @return
     */
    public static Pair<List<String>, List<String>> jsonContains(String json, String... paths) {
        Objects.requireNonNull(json);
        Objects.requireNonNull(paths);
        if (paths.length == 0)
            throw new IllegalArgumentException("Paths cannot be empty!");
        JsonElement jelem = FlareBot.GSON.fromJson(json, JsonElement.class);
        JSONConfig config = new JSONConfig(jelem.getAsJsonObject());
        List<String> contains = new ArrayList<>();
        List<String> notContains = new ArrayList<>();
        for (String path : paths) {
            if (path == null) continue;
            if (config.getElement(path).isPresent())
                contains.add(path);
            else
                notContains.add(path);
        }
        return new Pair<>(Collections.unmodifiableList(contains), Collections.unmodifiableList(notContains));
    }

    /**
     * Message fields:
     * - Message ID
     * - Author ID
     * - Channel ID
     * - Guild ID
     * - Raw Content
     * - Timestamp (Epoch seconds)
     *
     * @param message The message to serialise
     * @return The serialised message
     */
    public static String getRedisMessageJson(Message message) {
        return FlareBot.GSON.toJson(new RedisMessage(
                message.getId(),
                message.getAuthor().getId(),
                message.getChannel().getId(),
                message.getGuild().getId(),
                message.getContentRaw(),
                message.getCreationTime().toInstant().toEpochMilli()
        ));
    }

    public static RedisMessage toRedisMessage(String json) {
        Pair<List<String>, List<String>> paths = jsonContains(json,
                "messageID",
                "authorID",
                "channelID",
                "guildID",
                "content",
                "timestamp"
        );
        if (paths.getKey().size() != 6) {
            throw new IllegalArgumentException("Malformed JSON! Missing paths: " +
                    Arrays.toString(paths.getValue().toArray(new String[paths.getValue().size()])));
        }
        return FlareBot.GSON.fromJson(json, RedisMessage.class);
    }

    public static String getVerificationString(Guild.VerificationLevel level) {
        switch (level) {
            case HIGH:
                return "(\u256F\u00B0\u25A1\u00B0\uFF09\u256F\uFE35 \u253B\u2501\u253B"; //(╯°□°）╯︵ ┻━┻
            case VERY_HIGH:
                return "\u253B\u2501\u253B\u5F61 \u30FD(\u0CA0\u76CA\u0CA0)\u30CE\u5F61\u253B\u2501\u253B"; //┻━┻彡 ヽ(ಠ益ಠ)ノ彡┻━┻
            default:
                return level.toString().charAt(0) + level.toString().substring(1).toLowerCase();
        }
    }

    public static Long parseTime(String time) {
        Matcher digitMatcher = timeRegex.matcher(time);
        if (digitMatcher.matches()) {
            try {
                return new PeriodFormatterBuilder()
                        .appendHours().appendSuffix(":")
                        .appendMinutes().appendSuffix(":")
                        .appendSeconds()
                        .toFormatter()
                        .parsePeriod(time)
                        .toStandardDuration().getMillis();
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("m")
                .appendSeconds().appendSuffix("s")
                .toFormatter();
        Period period;
        try {
            period = formatter.parsePeriod(time);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return period.toStandardDuration().getMillis();
    }

}
