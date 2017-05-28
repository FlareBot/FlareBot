package stream.flarebot.flarebot.commands.secret;

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.util.MessageUtils;

import javax.security.auth.login.LoginException;

public class ShardRestartCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (getPermissions(channel).isCreator(sender)) {
            int shard = Integer.parseInt(args[0]);
            try {
                synchronized (FlareBot.getInstance().getClients()) {
                    FlareBot.getInstance().getClients()[shard].shutdown(false);
                    FlareBot.getInstance().getClients()[shard] = new JDABuilder(AccountType.BOT)
                            .addListener(FlareBot.getInstance().getEvents())
                            .useSharding(shard, FlareBot.getInstance().getClients().length)
                            .setToken(FlareBot.getToken())
                            .setAudioSendFactory(new NativeAudioSendFactory())
                            .buildAsync();
                }
            } catch (LoginException | RateLimitedException e) {
                MessageUtils.sendException("", e, channel);
            }
        }
    }

    @Override
    public String getCommand() {
        return "restart";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getUsage() {
        return "{%}restart <shard>";
    }

    @Override
    public CommandType getType() {
        return CommandType.HIDDEN;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }
}
