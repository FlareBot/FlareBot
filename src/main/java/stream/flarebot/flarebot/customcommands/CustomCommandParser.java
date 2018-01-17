package stream.flarebot.flarebot.customcommands;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.RichPresence;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Random;

/**
 * This is the parser for custom-commands, custom commands are something really nice that allows the user to create
 * their own commands with just about anything they need! There is a huge list of stuff the user can have their command do
 * including things like mention the user, get their game and play time plus a bunch bunch more.
 * <p>
 * <b>Some terminology to know:</b>
 * <ul>
 * <li>Variable - This is something which is enclosed in {} and allows for things like getting the user ID.
 * An example of a variable would be `{user.id}` which will be replaced with the user who ran the commands ID.</li>
 * <li>Process Variable - This is something which will be processed, that could be anything from randomly
 * picking, generating something or basically anything which isn't just serving data we have already.<br />
 * <b>These need data passed! To pass data put a `:` (colon) and the data to be processed after it.</b><br />
 * Example: `{choose:one;two;three}` - In this variable the "one;two;three" is data which will be processed.</li>
 * </ul>
 */
public class CustomCommandParser {

    private static final Random random = new Random();

    /**
     * Parse custom command text and variables into an actual resulting message and return that.
     * This method will parse and process all the variables it can along with returning the data which is given from them.<br />
     * To parse the variables this will run the {@link #parseVariables(String, User, TextChannel, GuildWrapper)} method.<br />
     * <b>Note: If there is a variable the bot doesn't know it will just spit out an unknown variable message!</b><br />
     * If you wish to escape a variable you can do so by escaping the `{}` for example: `\{command:userinfo \{user.id\}\}`
     * that will just print in plain text. If you do something like `\{command:userinfo {user.id}\} the `{user.id}`
     * <b>WILL</b> still be parsed.
     *
     * @param string  This is the custom command string which will need to be parsed and processed.
     * @param user    The user who ran the custom command, this is used for things like getting the ID and perm checking
     *                within in the bot.
     * @param channel The channel which this custom command was ran in, again is used for variables.
     * @param wrapper The {@link GuildWrapper} of the guild which ran the command. This is for member variables, prefix
     *                and other guild variables.
     * @return The parsed custom command.
     */
    public String parseCustomCommand(String string, User user, TextChannel channel, GuildWrapper wrapper) {
        String parsed = parseVariables(string, user, channel, wrapper);
        String output = parsed;

        int start = -1;
        for (int index = 0; index < parsed.length(); index++) {
            char c = parsed.charAt(index);
            if (index > 1000)
                System.out.println(index + ":" + c);

            if (c == '{' && parsed.charAt(index - 1) != '\\') {
                start = index;
                System.out.println("Set start.");
            }
            if (c == '}' && parsed.charAt(index - 1) != '\\' && start > 0) {
                System.out.println("Found end");
                String fullVariable = parsed.substring(start + 1, index);

                String variable = fullVariable;
                String variableData = null;
                if (fullVariable.contains(":")) {
                    variable = fullVariable.substring(0, fullVariable.indexOf(":"));
                    if (fullVariable.length() > fullVariable.indexOf(":"))
                        variableData = fullVariable.substring(fullVariable.indexOf(":") + 1);
                }

                // Process the variable
                System.out.println("Replacing: {" + fullVariable + "}");
                output = output.replace('{' + fullVariable + '}', processVariable(variable, variableData, user, channel, wrapper));

                System.out.println("fullVariable: " + fullVariable +
                        "\nvariable: " + variable +
                        "\nvariableData: " + variableData +
                        "\nIndex: " + index);
                System.out.println();
                start = -1; // Make sure to reset this!
            }
        }

        return output;
    }

    /**
     * @param msg
     * @param user
     * @param channel
     * @param wrapper
     * @return
     */
    private String parseVariables(String msg, User user, TextChannel channel, GuildWrapper wrapper) {
        Member member = wrapper.getGuild().getMember(user);
        Guild guild = wrapper.getGuild();
        return msg
                // User
                .replace("{user.id}", user.getId())
                .replace("{user.name}", user.getName())
                .replace("{user.nick}", wrapper.getGuild().getMember(user).getEffectiveName())
                .replace("{user.discriminator}", user.getDiscriminator())
                .replace("{user.tag}", user.getName() + '#' + user.getDiscriminator())
                .replace("{user.mention}", user.getAsMention())
                .replace("{user.game}", (member.getGame() != null ? member.getGame().getName() : "Nothing"))
                .replace("{user.gametime}", getGameTime(member))
                .replace("{user.avatar}", user.getEffectiveAvatarUrl())
                .replace("{user.createdAt}", GeneralUtils.formatDateTime(user.getCreationTime()))
                .replace("{user.joinedAt}", GeneralUtils.formatDateTime(member.getJoinDate()))

                // Server
                .replace("{server.id}", guild.getId())
                .replace("{server.name}", guild.getName())
                .replace("{server.icon}", guild.getIconUrl())
                .replace("{server.memberCount}", String.valueOf(guild.getMemberCache().size()))
                .replace("{server.ownerId}", guild.getOwner().getUser().getId())
                .replace("{server.createdAt}", GeneralUtils.formatDateTime(guild.getCreationTime()))
                .replace("{server.region}", guild.getRegion().getName())

                // Channel
                .replace("{channel.id}", channel.getId())
                .replace("{channel.name}", channel.getName())
                .replace("{channel.mention}", channel.getAsMention())
                .replace("{channel.topic}", channel.getTopic() != null ? channel.getTopic() : "No topic set!")

                // Advanced
                .replace("{prefix}", String.valueOf(wrapper.getPrefix()));
    }

    /**
     * @param member
     * @return
     */
    private String getGameTime(Member member) {
        if (member.getGame() != null && member.getGame().isRich()) {
            RichPresence richPresence = member.getGame().asRichPresence();
            if (richPresence.getTimestamps() != null)
                return richPresence.getTimestamps().getElapsedTime(ChronoUnit.MINUTES) + " minutes";
        }
        return "No time at all";
    }

    /**
     * @param variable
     * @param variableData
     * @param user
     * @param channel
     * @param wrapper
     * @return
     */
    private String processVariable(String variable, String variableData, User user, TextChannel channel, GuildWrapper wrapper) {
        switch (variable) {
            case "command":
                handleCommand(variableData, user, channel, wrapper);
                return "";
            case "choose":
            case "choice":
                return handleChoice(variableData);
            case "numgen":
            case "randomnum":
            case "randnum":
                int lower = 0;
                int higher = GeneralUtils.getInt(variableData, 10);
                if(variableData.contains("-")) {
                    lower = GeneralUtils.getInt(variableData.substring(0, variableData.indexOf("-")), 0);
                    higher = GeneralUtils.getInt(variableData.substring(variableData.indexOf("-") + 1), 10);
                }
                return String.valueOf(random.nextInt(higher) + lower);
            default:
                return "<><><>Error 1337505 - This should not have happened... report (`" + variable + "`) here: "
                        + FlareBot.INVITE_URL + "<><><>";
        }
    }

    /**
     * @param command
     * @param user
     * @param channel
     * @param wrapper
     */
    private void handleCommand(String command, User user, TextChannel channel, GuildWrapper wrapper) {
        String parsedCommand = parseVariables(command, user, channel, wrapper);
        String cmd = parsedCommand;
        String[] args = new String[0];
        if (parsedCommand.contains(" ")) {
            cmd = cmd.substring(0, parsedCommand.indexOf(" "));
            args = parsedCommand.substring(parsedCommand.indexOf(" ") + 1).split(" ");
        }
        FlareBot.getInstance().getCommand(cmd, user).onCommand(user, wrapper, channel,
                new MessageBuilder().append(parsedCommand).build(), args, wrapper.getGuild().getMember(user));
    }

    private String handleChoice(String msg) {
        System.out.println(msg);
        String[] choices = msg.split(";");
        System.out.println(Arrays.toString(choices));
        return choices[random.nextInt(choices.length)];
    }
}
