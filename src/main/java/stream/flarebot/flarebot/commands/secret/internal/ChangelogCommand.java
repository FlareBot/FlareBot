package stream.flarebot.flarebot.commands.secret.internal;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONObject;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.WebUtils;

import java.io.IOException;

public class ChangelogCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message msg, String[] args, Member member) {
        if (PerGuildPermissions.isStaff(sender)) {
            if (args.length == 0) {
                channel.sendMessage("Specify a version or PR to post about!").queue();
                return;
            }
            if (args[0].startsWith("pr:")) {
                String prNum = args[0].substring(3);

                JSONObject obj;
                try {
                    Response res = WebUtils.get("https://api.github.com/repos/FlareBot/FlareBot/pulls/" + prNum);
                    ResponseBody body = res.body();

                    if (body != null) {
                        obj = new JSONObject(body.string());
                        body.close();
                    } else {
                        res.close();
                        MessageUtils.sendErrorMessage("GitHub returned an empty response - Code " + res.code(),
                                channel, sender);
                        return;
                    }
                    res.close();
                } catch (IOException e) {
                    MessageUtils.sendErrorMessage("Error getting the PR info!\n" +
                            e.getMessage(), channel, sender);
                    return;
                }

                String body = obj.getString("body");
                String[] array = body.split("\r\n\r\n");

                EmbedBuilder embed = new EmbedBuilder();
                boolean hasTitle = false;
                for (String anArray : array) {
                    String value = anArray.replaceAll("\n\\* ", "\n\u2022 ");
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
                channel.sendMessage(new MessageBuilder().setEmbed(embed.build()).build()).queue();
            } else {
                String message = msg.getRawContent();
                message = message.substring(message.indexOf(" ") + 1);
                channel.sendMessage(message).queue();
            }
        }
    }

    @Override
    public String getCommand() {
        return "changelog";
    }

    @Override
    public String getDescription() {
        return "Get version changelogs";
    }

    @Override
    public String getUsage() {
        return "{%}changelog <version>\n`{%}changelog pr:<prNum>`";
    }

    @Override
    public CommandType getType() {
        return CommandType.INTERNAL;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"announcement"};
    }
}
