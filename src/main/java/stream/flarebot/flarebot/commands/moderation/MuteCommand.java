package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.TempManager;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

public class MuteCommand implements Command {
    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if(args.length == 0){
            MessageUtils.getUsage(this, channel, sender).queue();
        } else {
            User user = GeneralUtils.getUser(args[0], guild.getGuildId());
            if(user == null){
                MessageUtils.sendErrorMessage("Invalid user!!", channel);
                return;
            }
            if(args.length >= 2){
                String time = args[1];
                String length = time.substring(0, time.length() - 1);
                String key = time.substring(time.length() - 1);
                long mills;
                EmbedBuilder eb = new EmbedBuilder();
                try {
                    if(key.equals("s")){
                        mills = TimeUnit.SECONDS.toMillis(Long.parseLong(length));
                        eb.appendDescription("Muted " + user.getAsMention() + " for " + Long.parseLong(length) + " seconds");
                    } else if(key.equals("m")){
                        mills = TimeUnit.MINUTES.toMillis(Long.parseLong(length));
                        eb.appendDescription("Muted " + user.getAsMention() + " for " + Long.parseLong(length) + " minutes");
                    } else if(key.equals("h")){
                        mills = TimeUnit.HOURS.toMillis(Long.parseLong(length));
                        eb.appendDescription("Muted " + user.getAsMention() + " for " + Long.parseLong(length) + " hours");
                    } else if(key.equals("d")){
                        mills = TimeUnit.DAYS.toMillis(Long.parseLong(length));
                        eb.appendDescription("Muted " + user.getAsMention() + " for " + Long.parseLong(length) + " days");
                    } else {
                        MessageUtils.getUsage(this, channel, sender).queue();
                        return;
                    }
                } catch (NumberFormatException e){
                    MessageUtils.getUsage(this, channel, sender).queue();
                    return;
                }
                guild.getAutoModGuild().muteUser(guild.getGuild(), guild.getGuild().getMember(user));
                TempManager.add(guild.getGuild().getController().removeSingleRoleFromMember(guild.getGuild().getMember(user), guild.getMutedRole()), System.currentTimeMillis() + mills);
                eb.setColor(Color.CYAN);
                channel.sendMessage(eb.build()).queue();
            } else {
                guild.getAutoModGuild().muteUser(guild.getGuild(), guild.getGuild().getMember(user));
                EmbedBuilder eb = new EmbedBuilder();
                eb.appendDescription("Muted " + user.getAsMention());
                eb.setColor(Color.CYAN);
                channel.sendMessage(eb.build()).queue();
            }
        }
    }

    @Override
    public String getCommand() {
        return "mute";
    }

    @Override
    public String getDescription() {
        return "Mutes people";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}
