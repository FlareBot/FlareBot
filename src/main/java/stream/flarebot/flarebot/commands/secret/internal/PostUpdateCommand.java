package stream.flarebot.flarebot.commands.secret.internal;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;
import stream.flarebot.flarebot.util.GitHubUtils;

public class PostUpdateCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message msg, String[] args, Member member) {
        if (guild.getGuildId().equals("226785954537406464") && PerGuildPermissions.isStaff(sender)) {
            if (args.length == 0) {
                channel.sendMessage("You kinda need like.... a message to announce... like yeah...").queue();
                return;
            }

            Role r = guild.getGuild().getRoleById(320304080926801922L);
            r.getManager().setMentionable(true).queue(aVoid -> {
                if (args[0].startsWith("pr:")) {
                    channel.sendMessage(new MessageBuilder().setEmbed(GitHubUtils.getEmbedForPR(args[0].substring(3))
                            .build()).append(r.getAsMention()).build()).queue(bVoid ->
                            channel.sendMessage("make sure to report any bugs over to be <#242206261767176192> channel " +
                                    "and if you need any support we're happy to help over in the <#226786463440699392> " +
                                    "channel!").queue()
                    );

                } else {
                    String message = msg.getContentRaw();
                    message = message.substring(message.indexOf(" ") + 1);
                    channel.sendMessage(r.getAsMention() + "\n" + message).complete();
                }
            });
            r.getManager().setMentionable(false).queue();
        }
    }

    @Override
    public String getCommand() {
        return "postupdate";
    }

    @Override
    public String getDescription() {
        return "Dev only command";
    }

    @Override
    public String getUsage() {
        return "{%}postupdate [message]";
    }

    @Override
    public CommandType getType() {
        return CommandType.INTERNAL;
    }
}
