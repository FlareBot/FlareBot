package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.objects.PlayerCache;
import net.dv8tion.jda.core.entities.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class UserInfoCommand implements Command {

    private FlareBot flareBot = FlareBot.getInstance();
    private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("MMMM yyyy HH:mm:ss");

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        String id;
        if (args.length != 1)
            id = sender.getId();
        else id = args[0].replaceAll("[^0-9]", "");
        User user = FlareBot.getInstance().getUserByID(id);
        if (user == null) {
            MessageUtils.sendErrorMessage("We cannot find that user!", channel);
            return;
        }
        PlayerCache cache = flareBot.getPlayerCache(id);
        channel.sendMessage(MessageUtils.getEmbed(sender).addField("User Info", "User: " + user.getName() + "#" + user.getDiscriminator()
                    + "\nID: " + user.getId() + "\n" +
                    "Avatar: " + (user.getEffectiveAvatarUrl() != null ? "[`link`](" + user.getEffectiveAvatarUrl() + ')' : "None") + "\n" +
                    "Default Avatar: [`link`](" + MessageUtils.getDefaultAvatar(sender) + ')', true)
                    .addField("General Info",
                            "Servers: " + FlareBot.getInstance().getGuilds().stream().filter(guild -> guild.getMemberById(id) != null).count() + " shared\n" +
                                    "Roles: " + channel.getGuild().getMember(user).getRoles().stream()
                                    .map(Role::getName).collect(Collectors.joining(", ")), true)
                    .addField("Time Data", "Created: " + formatTime(LocalDateTime.from(user.getCreationTime())) + "\n" +
                            "Joined: " + formatTime(LocalDateTime.from(channel.getGuild().getMember(user).getJoinDate())) + "\n" +
                            "Last Seen: " + (cache.getLastSeen() == null ? "Unknown" : formatTime(cache.getLastSeen())) + "\n" +
                            "Last Spoke: " + (cache.getLastMessage() == null ? "Unknown" : formatTime(cache.getLastMessage())), false)
                    .setThumbnail(MessageUtils.getAvatar(user)).build()).queue();
    }

    @Override
    public String getCommand() {
        return "userinfo";
    }

    @Override
    public String getDescription() {
        return "Get information about a user";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    private String formatTime(LocalDateTime time) {
        return time.getDayOfMonth() + getDayOfMonthSuffix(time.getDayOfMonth()) + " " + time.format(timeFormat) + " UTC";
    }

    private String getDayOfMonthSuffix(final int n) {
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
}
