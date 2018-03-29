package stream.flarebot.flarebot.mod.nino;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.mod.modlog.ModlogEvent;
import stream.flarebot.flarebot.mod.modlog.ModlogHandler;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.Constants;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.FormatUtils;
import stream.flarebot.flarebot.util.general.VariableUtils;

import java.util.concurrent.atomic.AtomicReference;

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

            // Apply the channel whitelist.
            if (wrapper.getNINO().getWhitelistedChannels().contains(event.getChannel().getIdLong()))
                return;

            if (wrapper.getModeration().isEventEnabled(wrapper, ModlogEvent.NINO)) {
                AtomicReference<String> msg = new AtomicReference<>(FormatUtils.stripMentions(event.getMessage().getContentDisplay()));
                URLChecker.instance().checkMessage(wrapper, event.getChannel(), msg.get(), (flag, url) -> {
                    if (flag == null || url == null) return;

                    for (String s : wrapper.getNINO().getWhitelist()) {
                        if (url.equalsIgnoreCase(s))
                            return;
                    }

                    event.getMessage().delete().queue();

                    msg.set(FormatUtils.truncate(500, event.getMessage().getContentDisplay()));

                    EmbedBuilder eb = new EmbedBuilder()
                            .addField("Message", msg.get(), false)
                            .addField("Check", flag.toString(), true)
                            .addField("Site", url, true);

                    if (flag == URLCheckFlag.SUSPICIOUS) {
                        eb.addField(MessageUtils.ZERO_WIDTH_SPACE, "Message was removed due to a suspicious TLD, " +
                                "we delete ones which are commonly known to be used by spammers/scammers. " +
                                "Check this out for more info: https://www.spamhaus.org/statistics/tlds/" +
                                "\nIf you know this URL is perfectly fine and want us to whitelist it globally " +
                                "come to our " + Constants.INVITE_MARKDOWN + " otherwise you can just whitelist for your guild. " +
                                "Check out the NINO command for mor info.", false);
                    } else if (flag == URLCheckFlag.PHISHING) {
                        eb.addField(MessageUtils.ZERO_WIDTH_SPACE, "These are sites which we have found to phish for " +
                                "users personal information such as users account info or other. These are mainly " +
                                "ones we know about and have been seen used around Discord. If you wish to report a " +
                                "new one please join our " + Constants.INVITE_MARKDOWN, false);
                    }

                    ModlogHandler.getInstance().postToModlog(wrapper, ModlogEvent.NINO, event.getAuthor(),
                            eb.getFields().toArray(new MessageEmbed.Field[]{})
                    );
                });
            }
        }
    }
}
