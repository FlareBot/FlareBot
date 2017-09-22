package stream.flarebot.flarebot.commands.moderation.mod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.mod.Punishment;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

public class MuteCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender);
        } else {
            User user = GeneralUtils.getUser(args[0], guild.getGuildId());
            if (user == null) {
                MessageUtils.sendErrorMessage("Invalid user!!", channel);
                return;
            }
            // NOT FOR v4
//            if(args.length >= 2){
//                long mills;
//                EmbedBuilder eb = new EmbedBuilder();
//                PeriodFormatter formatter = new PeriodFormatterBuilder()
//                        .appendDays().appendSuffix("d")
//                        .appendHours().appendSuffix("h")
//                        .appendMinutes().appendSuffix("m")
//                        .appendSeconds().appendSuffix("s")
//                        .toFormatter();
//                Period period = formatter.parsePeriod(args[1]);
//                mills = period.toStandardSeconds().getSeconds() * 1000;
//                long seconds = mills / 1000;
//                long minutes = seconds / 60;
//                long hours = minutes / 60;
//                long days = hours / 24;
//                String time = days + " days " + hours % 24 + " hours " + minutes % 60 + " minutes " + seconds % 60 + " seconds.";
//                eb.appendDescription("Muted " + user.getAsMention() + " for " + time);
//                guild.getAutoModConfig().postToModLog(user, sender, new Punishment(Punishment.EPunishment.BAN), "");
//                guild.getAutoModGuild().muteUser(guild.getGuild(), guild.getGuild().getMember(user));
//                while (!guild.getGuild().getMember(user).getRoles().contains(guild.getMutedRole())){
            //nothing!
//                }
//                RestAction ra = guild.getGuild().getController().removeSingleRoleFromMember(
//                        guild.getGuild().getMember(user), guild.getMutedRole());
//                new RestActionTask(ra, "Unmute Member: " + user.getName()).delay(mills);
//                eb.setColor(Color.CYAN);
//                channel.sendMessage(eb.build()).queue();
//            } else {

            if (guild.getMutedRole() == null) {
                MessageUtils.sendErrorMessage("Error getting the \"Muted\" role! Check FlareBot has permissions to create it!", channel);
                return;
            }
            guild.getAutoModGuild().muteUser(guild.getGuild(), guild.getGuild().getMember(user));
            String reason = args.length > 1 ? FlareBot.getMessage(args, 1) : null;
            guild.getAutoModConfig().postToModLog(user, sender, new Punishment(Punishment.EPunishment.MUTE), reason);
            EmbedBuilder eb = new EmbedBuilder();
            eb.appendDescription("Muted " + user.getAsMention());
            eb.setColor(Color.CYAN);
            channel.sendMessage(eb.build()).queue();
//            }
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
        return "`{%}mute <user>` - Mutes a user";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}
