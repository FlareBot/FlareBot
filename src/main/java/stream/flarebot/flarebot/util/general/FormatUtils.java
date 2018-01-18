package stream.flarebot.flarebot.util.general;

import net.dv8tion.jda.core.entities.Guild;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import stream.flarebot.flarebot.objects.GuildWrapper;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class FormatUtils {
    private static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("MMMM yyyy HH:mm:ss");

    private static final SimpleDateFormat preciseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");

    private static final PeriodFormatter prettyTime = new PeriodFormatterBuilder()
            .appendDays().appendSuffix(" Day ", " Days ")
            .appendHours().appendSuffix(" Hour ", " Hours ")
            .appendMinutes().appendSuffix(" Minute ", " Minutes ")
            .appendSeconds().appendSuffix(" Second", " Seconds")
            .toFormatter();

    /**
     * Formats a duration.
     *
     * @param duration The duration in millis to format.
     * @return A string that is the duration.
     */
    public static String formatDuration(long duration) {
        long totalSeconds = duration / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = (totalSeconds / 3600);
        return (hours > 0 ? (hours < 10 ? "0" + hours : hours) + ":" : "")
                + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    /**
     * Formats a String to replace {%} with the prefix.
     *
     * @param guild The {@link Guild} the get prefix from.
     * @param usage The usage String.
     * @return The String with the prefix instead of {%}.
     */
    public static String formatCommandPrefix(Guild guild, String usage) {
        String prefix = String.valueOf(GuildUtils.getPrefix(guild));
        if (usage.contains("{%}"))
            return usage.replaceAll("\\{%}", prefix);
        return usage;
    }

    /**
     * Formats a String to replace {%} with the prefix.
     *
     * @param guild The guild wrapper for the {@link Guild} the get prefix from.
     * @param usage The usage String.
     * @return The String with the prefix instead of {%}.
     */
    public static String formatCommandPrefix(GuildWrapper guild, String usage) {
        String prefix = String.valueOf(GuildUtils.getPrefix(guild));
        if (usage.contains("{%}"))
            return usage.replaceAll("\\{%}", prefix);
        return usage;
    }

    /**
     * Formats a color into a String
     *
     * @param color The color to format
     * @return The hex value of the color
     */
    public static String colourFormat(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static String formatJodaTime(Period period) {
        return period.toString(FormatUtils.prettyTime).trim();
    }

    /**
     * This will format a Joda Period into a precise timestamp (yyyy-MM-dd HH:mm:ss.SS).
     *
     * @param period Period to format onto the current date
     * @return The date in a precise format. Example: 2017-10-13 21:56:33.681
     */
    public static String formatPrecisely(Period period) {
        return FormatUtils.preciseFormat.format(DateTime.now(DateTimeZone.UTC).plus(period).toDate());
    }

    /**
     * Formats a {@link LocalDateTime}
     *
     * @param dateTime The {@link LocalDateTime} to format.
     * @return A String repressing the time.
     */
    public static String formatTime(LocalDateTime dateTime) {
        LocalDateTime time = LocalDateTime.from(dateTime.atOffset(ZoneOffset.UTC));
        return time.getDayOfMonth() + GeneralUtils.getDayOfMonthSuffix(time.getDayOfMonth()) + " " + time
                .format(FormatUtils.timeFormat) + " UTC";
    }

    /**
     * Formats time.
     *
     * @param duration The duration to format.
     * @param durUnit The unit the duration is in.
     * @param fullUnits Weather not not to return full units. ex days instead of d.
     * @param append0 Weather or not to append 0s
     * @return The formatted time.
     */
    public static String formatTime(long duration, TimeUnit durUnit, boolean fullUnits, boolean append0) {
        long totalSeconds = 0;
        switch (durUnit) {
            case MILLISECONDS:
                totalSeconds = duration / 1000;
                break;
            case SECONDS:
                totalSeconds = duration;
                break;
            case MINUTES:
                totalSeconds = duration * 60;
                break;
            case HOURS:
                totalSeconds = (duration * 60) * 60;
                break;
            case DAYS:
                totalSeconds = ((duration * 60) * 60) * 24;
                break;
        }
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = (totalSeconds / 3600) % 24;
        long days = (totalSeconds / 86400);
        return (days > 0 ? (append0 && days < 10 ? "0" + days : days) + (fullUnits ? " days " : "d ") : "")
                + (hours > 0 ? (append0 && hours < 10 ? "0" + hours : hours) + (fullUnits ? " hours " : "h ") : "")
                + (minutes > 0 ? (append0 && minutes < 10 ? "0" + minutes : minutes) + (fullUnits ? " minutes" : "m ") : "")
                + (seconds > 0 ? (append0 && seconds < 10 ? "0" + seconds : seconds) + (fullUnits ? " seconds" : "s") : "")
                .trim();
    }

    /**
     * Truncates a String to the given length.
     * With ellipses.
     *
     * @param length The amount to add.
     * @param string The string to add them to.
     * @return The truncated String.
     */
    public static String truncate(int length, String string) {
        return truncate(length, string, true);
    }

    /**
     * Truncates a String to the given length.
     *
     * @param length The amount to add.
     * @param string The string to add them to.
     * @param ellipse Weather or not to use ellipses.
     * @return The truncated String.
     */
    public static String truncate(int length, String string, boolean ellipse) {
        return string.substring(0, Math.min(string.length(), length - (ellipse ? 3 : 0))) + (string.length() >
                length - (ellipse ? 3 : 0) && ellipse ? "..." : "");
    }
}
