package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONObject;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.WebUtils;

import java.io.IOException;

public class PostUpdateCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message msg, String[] args, Member member) {
        if (guild.getGuildId().equals("226785954537406464")) {
            if (args.length == 0) {
                channel.sendMessage("You kinda need like.... a message to announce... like yeah...").queue();
                return;
            }

            Role r = guild.getGuild().getRoleById(320304080926801922L);
            r.getManager().setMentionable(true).queue(aVoid -> {
                if (args[0].startsWith("pr:")) {
                    String prNum = args[0].substring(3);

                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(WebUtils.get("https://api.github.com/repos/FlareBot/FlareBot/pulls/" + prNum).body().string());
                    } catch (IOException e) {
                        MessageUtils.sendErrorMessage("Error getting the PR info!\n" +
                                e.getMessage(), channel, sender);
                        return;
                    }

                    String body = obj.getString("body");
                    String[] array = body.split("\r\n\r\n");

                    EmbedBuilder embed = new EmbedBuilder();
                    boolean hasTitle = false;
                    for (int i = 0; i < array.length; i++) {
                        String value = array[i].replaceAll("\n\\* ", "\n• "); // this breaks markdown...
                        String header = value.replace("## ", "").substring(0, value.indexOf("\n") - 4).replace("\n", "");

                        value = value.replace("## " + header, "");

                        if (!hasTitle) {
                            embed.setTitle(header, null);
                            embed.setDescription(value);
                            hasTitle = true;
                            continue;
                        }

                        if (value.length() > 1024) {
                            embed.addField(header, value.substring(0, value.substring(0, 1024).lastIndexOf("\n")), false);
                            value = value.substring(value.substring(0, 1024).lastIndexOf("\n") + 1);
                            header += " - Continued";
                        }

                        embed.addField(header, value, false);
                    }
                    channel.sendMessage(new MessageBuilder().setEmbed(embed.build()).append(r.getAsMention()).build()).queue();
                } else {
                    String message = msg.getRawContent();
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
        return "{%}announcement [message]";
    }

    @Override
    public CommandType getType() {
        return CommandType.SECRET;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"announcement"};
    }
}
