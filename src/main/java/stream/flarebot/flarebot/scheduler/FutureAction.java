package stream.flarebot.flarebot.scheduler;

import com.datastax.driver.core.PreparedStatement;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.database.CassandraController;
import stream.flarebot.flarebot.mod.modlog.ModAction;
import stream.flarebot.flarebot.mod.modlog.ModlogHandler;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;

public class FutureAction {

    private static PreparedStatement update;
    private static PreparedStatement delete;

    /*
     * Ok so this will work with a few things, due to this it will have quite a few weird fields.
     *
     * CREATE TABLE future_tasks (guild_id bigint, channel_id bigint, responsible bigint, content text,
     *                            expires_at timestamp, created_at timestamp, action varchar)
     */

    /**
     * Guild ID it was executed in
     */
    private long guildId;
    /**
     * Channel ID it was executed in
     */
    private long channelId;
    /**
     * ID of user who ran the command - Could be a mod or normal user.
     */
    private long responsible;
    /**
     * ID of the target user - if applicable
     */
    private long target;
    /**
     * Content - This could be a reason or just a general message.
     */
    private String content;
    /**
     * How long until the action will be executed.
     */
    private Period delay;
    /**
     * When the task expires. This is calculated from the delay, it will be added onto the current time.
     */
    private DateTime expires;
    /**
     * When the task was created
     */
    private DateTime created;
    /**
     * Rest action - This could be a role remove, a message being sent etc.
     */
    private Action action;

    public FutureAction(long guildId, long channelId, long responsible, long target, String content,
                        DateTime expires, DateTime created, Action action) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.responsible = responsible;
        this.target = target;
        this.content = content;
        this.delay = new Period(expires.minus(created.getMillis()).getMillis());
        this.expires = expires;
        this.created = created;
        this.action = action;
    }

    public FutureAction(long guildId, long channelId, long responsible, long target, String content,
                        Period delay, Action action) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.responsible = responsible;
        this.target = target;
        this.content = content;
        this.delay = delay;
        this.created = new DateTime(DateTimeZone.UTC);
        this.expires = created.plus(delay);
        this.action = action;
    }

    public FutureAction(long guildId, long channelId, long responsible, String content, Period delay, Action action) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.responsible = responsible;
        this.target = -1;
        this.content = content;
        this.delay = delay;
        this.created = new DateTime(DateTimeZone.UTC);
        this.expires = created.plus(delay);
        this.action = action;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getChannelId() {
        return channelId;
    }

    public long getResponsible() {
        return responsible;
    }

    public long getTarget() {
        return this.target;
    }

    public String getContent() {
        return content;
    }

    public DateTime getExpires() {
        return expires;
    }

    public DateTime getCreated() {
        return created;
    }

    public Action getAction() {
        return action;
    }

    public void execute() {
        GuildWrapper gw = FlareBotManager.getInstance().getGuild(String.valueOf(guildId));
        switch (action) {
            case TEMP_MUTE:
                ModlogHandler.getInstance().handleAction(gw,
                        ModlogHandler.getInstance().getModlogChannel(gw, ModAction.UNMUTE.getEvent()),
                        null,
                        GeneralUtils.getUser(String.valueOf(target), String.valueOf(guildId), true),
                        ModAction.UNMUTE,
                        "Temporary mute expired, was muted for " + GeneralUtils.formatJodaTime(delay)
                );
                break;
            case TEMP_BAN:
                ModlogHandler.getInstance().handleAction(gw,
                        ModlogHandler.getInstance().getModlogChannel(gw, ModAction.UNBAN.getEvent()),
                        null,
                        GeneralUtils.getUser(String.valueOf(target), String.valueOf(guildId), true),
                        ModAction.UNBAN,
                        "Temporary ban expired, was banned for " + GeneralUtils.formatJodaTime(delay)
                );
                break;
            case REMINDER:
                if (FlareBot.getInstance().getChannelById(channelId) != null)
                    FlareBot.getInstance().getChannelById(channelId).sendMessage(FlareBot.getInstance()
                            .getUserById(responsible).getAsMention() + " You asked me to remind you " +
                            GeneralUtils.formatJodaTime(delay).toLowerCase() + " ago about: `" + content.replaceAll("`", "'") + "`")
                            .queue();
                break;
            default:
                break;
        }
        delete();
    }

    public void queue() {
        // I have to minus here since this has the complete end time.
        Scheduler.delayTask(this::execute, "FutureTask-" + action.name() + "-" + expires.toString(),
                getExpires().minus(System.currentTimeMillis()).getMillis());
        if (update == null) update = CassandraController.prepare("UPDATE flarebot.future_tasks SET responsible = ?, " +
                "target = ?, content = ?, expires_at = ?, action = ? WHERE guild_id = ? AND channel_id = ? " +
                "AND created_at = ?");
        CassandraController.executeAsync(update.bind().setLong(0, responsible).setLong(1, target).setString(2, content)
                .setTimestamp(3, expires.toDate()).setString(4, action.name()).setLong(5, guildId).setLong(6, channelId)
                .setTimestamp(7, created.toDate()));
    }

    public void delete() {
        if (delete == null)
            delete = CassandraController.prepare("DELETE FROM flarebot.future_tasks WHERE guild_id = ? " +
                    "AND channel_id = ? AND created_at = ?");
        CassandraController.executeAsync(delete.bind().setLong(0, guildId).setLong(1, channelId)
                .setTimestamp(2, created.toDate()));
    }

    public enum Action {
        TEMP_MUTE,
        TEMP_BAN,
        REMINDER
    }
}
