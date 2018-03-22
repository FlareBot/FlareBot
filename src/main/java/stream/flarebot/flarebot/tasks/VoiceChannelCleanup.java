package stream.flarebot.flarebot.tasks;

import com.arsenarsen.lavaplayerbridge.player.Player;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Getters;
import stream.flarebot.flarebot.metrics.Metrics;
import stream.flarebot.flarebot.scheduler.FlareBotTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class VoiceChannelCleanup extends FlareBotTask {

    private static final Logger logger = LoggerFactory.getLogger(VoiceChannelCleanup.class);
    private static final Map<Long, Long> VC_LAST_USED = new HashMap<>();
    private static final long CLEANUP_THRESHOLD = 15 * 60 * 1000;

    public VoiceChannelCleanup(String taskName) {
        super(taskName, TimeUnit.MINUTES.toMillis(20), TimeUnit.MINUTES.toMillis(15));
    }

    @Override
    public void run() {
        cleanup();
    }

    private void cleanup() {
        logger.info("Checking for guilds with an inactive voice channel");
        final AtomicInteger totalGuilds = new AtomicInteger(0);
        final AtomicInteger totalVcs = new AtomicInteger(0);
        final AtomicInteger killedVcs = new AtomicInteger(0);

        Getters.getGuildCache().forEach(guild -> {
            try {
                totalGuilds.incrementAndGet();

                if (guild != null && guild.getSelfMember() != null && guild.getSelfMember().getVoiceState() != null
                        && guild.getSelfMember().getVoiceState().getChannel() != null) {
                    totalVcs.incrementAndGet();

                    VoiceChannel vc = guild.getSelfMember().getVoiceState().getChannel();

                    if (getHumansInChannel(vc) == 0) {
                        killedVcs.incrementAndGet();
                        VC_LAST_USED.remove(vc.getIdLong());
                        guild.getAudioManager().closeAudioConnection();
                    } else if (isPlayingMusic(vc)) {
                        VC_LAST_USED.put(vc.getIdLong(), System.currentTimeMillis());
                    } else {
                        if (!VC_LAST_USED.containsKey(vc.getIdLong())) {
                            VC_LAST_USED.put(vc.getIdLong(), System.currentTimeMillis());
                            return;
                        }

                        long lastUsed = VC_LAST_USED.get(vc.getIdLong());

                        if (System.currentTimeMillis() - lastUsed >= CLEANUP_THRESHOLD) {
                            killedVcs.incrementAndGet();
                            guild.getAudioManager().closeAudioConnection();
                            FlareBot.instance().getMusicManager().getPlayer(guild.getIconId()).getPlaylist().clear();
                            VC_LAST_USED.remove(vc.getIdLong());
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to check {} for inactive voice connection!", guild.getIdLong(), e);
            }
        });

        logger.info("Checked {} guilds for inactive voice channels.", totalGuilds.get());
        logger.info("Killed {} out of {} voice connections!", killedVcs.get(), totalVcs.get());
        Metrics.voiceChannelsCleanedUp.inc(killedVcs.get());
    }

    private int getHumansInChannel(VoiceChannel channel) {
        int i = 0;
        for (Member m : channel.getMembers())
            if (!m.getUser().isBot())
                i++;
        return i;
    }

    private boolean isPlayingMusic(VoiceChannel vc) {
        Player player = FlareBot.instance().getMusicManager().getPlayer(vc.getGuild().getId());
        return player != null && !player.getPaused() && player.getPlayingTrack() != null;
    }
}
