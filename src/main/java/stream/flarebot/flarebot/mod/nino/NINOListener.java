package stream.flarebot.flarebot.mod.nino;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.mod.modlog.ModlogEvent;
import stream.flarebot.flarebot.mod.modlog.ModlogHandler;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.FormatUtils;
import stream.flarebot.flarebot.util.general.VariableUtils;

public class NINOListener implements EventListener {

    @Override
    public void onEvent(Event event) {
        if (event instanceof GuildMessageReceivedEvent)
            onGuildMessageReceived((GuildMessageReceivedEvent) event);
    }

    private void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.getMember().hasPermission(event.getChannel(), Permission.MESSAGE_MANAGE)) return;

        GuildWrapper wrapper = FlareBotManager.instance().getGuild(event.getGuild().getId());
        if (wrapper.getNINO().isEnabled()) {
            // This event is deprecated and will be removed fully soon!
            if (!wrapper.getModeration().isEventEnabled(wrapper, ModlogEvent.INVITE_POSTED) &&
                    !wrapper.getModeration().isEventEnabled(wrapper, ModlogEvent.NINO)) return;

            if (wrapper.getModeration().isEventEnabled(wrapper, ModlogEvent.NINO)) {
                URLChecker.instance().checkMessage(wrapper, event.getMessage().getContentDisplay(), (flag, url) -> {
                    if (flag == null || url == null) return;
                    String msg = FormatUtils.truncate(500, event.getMessage().getContentDisplay());

                    ModlogHandler.getInstance().postToModlog(wrapper, ModlogEvent.NINO, event.getAuthor(),
                            new MessageEmbed.Field("Message", msg, false),
                            new MessageEmbed.Field("Check", flag.toString(), true),
                            new MessageEmbed.Field("Site", url, true)
                    );
                });
            }

            runLegacyInvite(wrapper, event);
        }
    }

    private void runLegacyInvite(GuildWrapper wrapper, GuildMessageReceivedEvent event) {
        String invite = MessageUtils.getInvite(event.getMessage().getContentRaw());
        if (invite == null) return;
        for (String inv : wrapper.getNINO().getWhitelist())
            if (invite.equalsIgnoreCase(inv))
                return;

        event.getMessage().delete().queue(aVoid -> {
            String msg = wrapper.getNINO().getRemoveMessage();
            if (msg != null)
                event.getChannel().sendMessage(
                        VariableUtils.parseVariables(msg, wrapper, event.getChannel(), event.getAuthor())
                ).queue();
        });

        if (wrapper.getModeration().isEventEnabled(wrapper, ModlogEvent.INVITE_POSTED)
                && !wrapper.getModeration().isEventEnabled(wrapper, ModlogEvent.NINO))
            ModlogHandler.getInstance().postToModlog(wrapper, ModlogEvent.INVITE_POSTED, event.getAuthor(),
                    new MessageEmbed.Field("Invite", invite, false),
                    new MessageEmbed.Field("Deprecated", "This event has been deprecated in favour of the `NINO` " +
                            "event! Please enable that event!", false)
            );
        else {
            ModlogHandler.getInstance().postToModlog(wrapper, ModlogEvent.NINO, event.getAuthor(),
                    new MessageEmbed.Field("Invite", invite, false)
            );
        }
    }
}
