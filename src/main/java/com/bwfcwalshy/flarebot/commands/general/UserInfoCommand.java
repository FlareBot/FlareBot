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
                    .addField("Time Data", "Created: " + flareBot.formatTime(LocalDateTime.from(user.getCreationTime())) + "\n" +
                            "Joined: " + flareBot.formatTime(LocalDateTime.from(channel.getGuild().getMember(user).getJoinDate())) + "\n" +
                            "Last Seen: " + (cache.getLastSeen() == null ? "Unknown" : flareBot.formatTime(cache.getLastSeen())) + "\n" +
                            "Last Spoke: " + (cache.getLastMessage() == null ? "Unknown" : flareBot.formatTime(cache.getLastMessage())), false)
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
}
