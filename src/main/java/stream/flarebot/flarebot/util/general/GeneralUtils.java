package stream.flarebot.flarebot.util.general;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.google.gson.JsonElement;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import io.github.binaryoverload.JSONConfig;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.Getters;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.database.RedisMessage;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.Report;
import stream.flarebot.flarebot.objects.ReportMessage;
import stream.flarebot.flarebot.util.Constants;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.Pair;
import stream.flarebot.flarebot.util.errorhandling.Markers;
import stream.flarebot.flarebot.util.implementations.MultiSelectionContent;

import java.awt.Color;
import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;

public class GeneralUtils {

    //Constants
    private static final DecimalFormat percentageFormat = new DecimalFormat("#.##");
    private static final Pattern timeRegex = Pattern.compile("^([0-9]*):?([0-9]*)?:?([0-9]*)?$");

    private static final PeriodFormatter periodParser = new PeriodFormatterBuilder()
            .appendDays().appendSuffix("d")
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .appendSeconds().appendSuffix("s")
            .toFormatter();


    /**
     * Gets a user count for a guild not including bots
     *
     * @param guild The guild to get the user count for
     * @return The amount of non-bot users in a guild
     */
    public static int getGuildUserCount(Guild guild) {
        int i = 0;
        for (Member member : guild.getMembers()) {
            if (!member.getUser().isBot()) {
                i++;
            }
        }
        return i;
    }

    /**
     * Gets the {@link Report} embed with all of the info on the report.
     *
     * @param sender The {@link User} who requested the embed
     * @param report The {@link Report} to get the embed of.
     * @return an {@link EmbedBuilder} that contains all the report data
     */
    public static EmbedBuilder getReportEmbed(User sender, Report report) {
        EmbedBuilder eb = MessageUtils.getEmbed(sender);
        User reporter = Getters.getUserById(report.getReporterId());
        User reported = Getters.getUserById(report.getReportedId());

        eb.addField("Report ID", String.valueOf(report.getId()), true);
        eb.addField("Reporter", reporter != null ? MessageUtils.getTag(reporter) : "Unknown", true);
        eb.addField("Reported", reported != null ? MessageUtils.getTag(reported) : "Unknown", true);

        //eb.addField("Time", report.getTime().toLocalDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " GMT/BST", true);
        eb.setTimestamp(report.getTime().toLocalDateTime());
        eb.addField("Status", report.getStatus().getMessage(), true);

        eb.addField("Message", "```" + report.getMessage() + "```", false);
        StringBuilder builder = new StringBuilder("The last 5 messages by the reported user: ```\n");
        for (ReportMessage m : report.getMessages()) {
            builder.append("[").append(m.getTime().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append(" GMT/BST] ")
                    .append(FormatUtils.truncate(100, m.getMessage()))
                    .append("\n");
        }
        builder.append("```");
        eb.addField("Messages from reported user", builder.toString(), false);
        return eb;
    }

    /**
     * Gets the string representing the current page out of total.
     * Example: 6/10.
     *
     * @param page       The current page.
     * @param items      The items that are being paged.
     * @param pageLength The page length.
     * @return A string representing the current page out of the total pages which was calculated from the items and page length.
     */
    public static String getPageOutOfTotal(int page, List<?> items, int pageLength) {
        return page + "/" + String.valueOf(items.size() < pageLength ? 1 : (items.size() / pageLength) + (items.size() % pageLength != 0 ? 1 : 0));
    }

    /**
     * Gets the progress bar for the current {@link Track} including the percent played.
     *
     * @param track The {@link Track} to get a progress bar for.
     * @return A string the represents a progress bar that represents the time played.
     */
    public static String getProgressBar(Track track) {
        float percentage = (100f / track.getTrack().getDuration() * track.getTrack().getPosition());
        return "[" + StringUtils.repeat("▬", (int) Math.round((double) percentage / 10)) +
                "](https://github.com/FlareBot)" +
                StringUtils.repeat("▬", 10 - (int) Math.round((double) percentage / 10)) +
                " " + GeneralUtils.percentageFormat.format(percentage) + "%";
    }

    /**
     * Gets a String stacktrace from a {@link Throwable}.
     * This return the String you'd typical see with printStackTrace()
     *
     * @param e the {@link Throwable}.
     * @return A string representing the stacktrace.
     */
    public static String getStackTrace(Throwable e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        printWriter.close();
        return writer.toString();
    }

    /**
     * Gets an int from a String.
     * The default value you pass is what it return if their was an error parsing the string.
     * This happens when the string you enter isn't an int. For example if you enter in 'no'.
     *
     * @param s            The string to parse an int from.
     * @param defaultValue The default int value to get in case parsing fails.
     * @return The int parsed from the string or the default value.
     */
    public static int getInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets an int from a String.
     * The default value you pass is what it return if their was an error parsing the string.
     * This happens when the string you enter isn't an int. For example if you enter in 'no'.
     *
     * @param s            The string to parse an int from.
     * @param defaultValue The default int value to get in case parsing fails.
     * @return The int parsed from the string or the default value.
     */
    public static int getPositiveInt(String s, int defaultValue) {
        try {
            int i = Integer.parseInt(s);
            return (i >= 0 ? i : defaultValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets a long from a String.
     * The default value you pass is what it return if their was an error parsing the string.
     * This happens when the string you enter isn't an int. For example if you enter in 'no'.
     *
     * @param s            The string to parse a long from.
     * @param defaultValue The default long value to get in case parsing fails.
     * @return The long parsed from the string or the default value.
     */
    public static long getLong(String s, long defaultValue) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get a Joda {@link Period} from the input string.
     * This will convert something like `1d20s` to 1 day and 20 seconds.
     * If the format isn't correct we throw an error message to the channel.
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

    /**
     * Gets the changes between two lists.
     * Represented by a map where the true value is the things added and the false
     * is the things removed with the unchanged being left out.
     *
     * @param oldList The old list
     * @param newList The new List
     * @return A map where the added objects are true, and the removed objects are false. the values the same are not included.
     */
    public static <T> Map<Boolean, List<T>> getChanged(List<T> oldList, List<T> newList) {
        Map<Boolean, List<T>> changes = new HashMap<>();
        List<T> removed = new ArrayList<>();
        List<T> added = new ArrayList<>();
        for (T oldItem : oldList) {
            if (!newList.contains(oldItem)) {
                removed.add(oldItem);
            }
        }
        for (T newItem : newList) {
            if (!oldList.contains(newItem)) {
                added.add(newItem);
            }
        }
        changes.put(true, added);
        changes.put(false, removed);
        return changes;
    }

    /**
     * Gets the suffix for the a day in a month
     * Example: 1st
     *
     * @param n The day in the month to get a suffix.
     * @return The suffix for the day.
     */
    public static String getDayOfMonthSuffix(final int n) {
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

    /**
     * Coverts the json from redis to a RedisMessage.
     * Message fields:
     * - Message ID
     * - Author ID
     * - Channel ID
     * - Guild ID
     * - Raw Content
     * - Timestamp (Epoch seconds)
     *
     * @param json The json to convert
     * @return {@link RedisMessage}
     * @throws IllegalArgumentException This throws if the json doesn't match what {@link RedisMessage} is expecting.
     */
    public static RedisMessage toRedisMessage(String json) throws IllegalArgumentException {
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

    /**
     * Gets the String representing the verification level.
     * For {@link Guild.VerificationLevel#HIGH} and {@link Guild.VerificationLevel#VERY_HIGH}
     * we return the unicode characters and not the string it's self.
     *
     * @param level The verification level to get the string version.
     * @return A string representing the verification level.
     */
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

    /**
     * Parses time from string.
     * The input can be in these formats
     * s
     * m:s
     * h:m:s
     * XhXmXs
     * Examples:
     * 5 - Would be 5 seconds
     * 5s - Would also be 5 seconds
     * 1:10 - Would be 1 minute and 10 seconds
     * 1m10s - Would also be 1 minute and 10 seconds
     * 1:00:00 - Would be an hour
     * 1h - Would also be an hour
     *
     * @param time The time string to parse.
     * @return The long representing the time entered in millis from when this method was ran.
     */
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

    /**
     * Converts an {@link MessageEmbed} into a String.
     * This will first start with the title in bold. then adds the description.
     * Then puts in all the fields with the title fallowed by the value. The finally the footer italicized.
     *
     * @param embed The {@link MessageEmbed} to convert
     * @return The String containing embed data
     */
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

    /**
     * Resolves an {@link AudioItem} from a string.
     * This can be a url or search terms
     *
     * @param player The music player
     * @param input  The string to get the AudioItem from.
     * @return {@link AudioItem} from the string.
     * @throws IllegalArgumentException If the Item couldn't be found due to it not existing on Youtube.
     * @throws IllegalStateException    If the Video is unavailable for Flare, for example if it was published by VEVO.
     */
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

    public static String truncate(int length, String string) {
        return truncate(length, string, true);
    }

    public static String truncate(int length, String string, boolean ellipse) {
        return string.substring(0, Math.min(string.length(), length - (ellipse ? 3 : 0))) + (string.length() >
                length - (ellipse ? 3 : 0) && ellipse ? "..." : "");
    }

    @Deprecated
    @Nullable
    public static TextChannel getChannel(String arg) {
        return getChannel(arg, null);
    }

    @Deprecated
    @Nullable
    public static TextChannel getChannel(String channelArg, GuildWrapper wrapper) {
        return GuildUtils.getChannel(channelArg, wrapper);
    }

    /**
     * Orders a Collection alphabetic by whatever {@link String#valueOf(Object)} returns.
     *
     * @param strings The Collection to order.
     * @return The ordered List.
     */
    public static <T extends Comparable> List<T> orderList(Collection<? extends T> strings) {
        List<T> list = new ArrayList<>(strings);
        list.sort(Comparable::compareTo);
        return list;
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

    /**
     * Runs code and catches any errors. It prints success, error and start messages as appropriate to the specified logger.
     *
     * @param logger         The logger to log to.
     * @param startMessage   The start message. Gets sent to the logger when the runnable starts.
     * @param successMessage The success message. Gets sent to the logger if the runnable finished successfully.
     * @param errorMessage   The error message. Gets sent to the logger if an error is thrown in the runnable.
     * @param runnable       The {@link Runnable} to run.
     */
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
     * This is to handle "multi-selection commands" for example the info and stats commands which take one or more
     * arguments and get select data from an enum
     */
    public static void handleMultiSelectionCommand(User sender, TextChannel channel, String[] args,
                                                   MultiSelectionContent<String, String, Boolean>[] providedContent) {
        String search = MessageUtils.getMessage(args);
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
     * Returns a lookup map for an enum, using the passed transform function.
     *
     * @param clazz  The clazz of the enum
     * @param mapper The mapper. Must be bijective as it otherwise overwrites keys/values.
     * @param <T>    the enum type
     * @param <R>    the type of map key
     * @return a map with the given key and the enum value associated with it
     * @apiNote Thanks to I Al Istannen#1564 for this
     */
    public static <T extends Enum, R> Map<R, T> getReverseMapping(Class<T> clazz, Function<T, R> mapper) {
        Map<R, T> result = new HashMap<>();

        for (T t : clazz.getEnumConstants()) {
            result.put(mapper.apply(t), t);
        }

        return result;
    }

    public static boolean canRunCommand(@Nonnull Command command, User user) {
        return canRunCommand(command.getType(), user);
    }

    /**
     * If the user can run the command, this will check if the command is null and if it is internal.
     * If internal it will check the official guild to see if the user has the right role.
     * <b>This does not check permissions</b>
     *
     * @return If the command is not internal or if the role has the right role to run an internal command.
     */
    public static boolean canRunCommand(@Nonnull CommandType type, User user) {
        if (type.isInternal()) {
            Guild g = Getters.getOfficialGuild();

            if (g.getMember(user) != null) {
                for (long roleId : type.getRoleIds())
                    if (g.getMember(user).getRoles().contains(g.getRoleById(roleId)))
                        return true;
            } else
                return false;
        }
        return true;
    }
}
