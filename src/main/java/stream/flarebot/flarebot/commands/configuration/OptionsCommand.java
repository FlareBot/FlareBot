package stream.flarebot.flarebot.commands.configuration;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.guilds.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OptionsCommand implements Command {

    /*

        _options set <option> <value>
                 get <option>
                 list

     */

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            Map<String, List<String>> options = new HashMap<>();
            for (String key : guild.getOptions().getOptions().keySet()) {
                String category = key.split("\\.")[0];
                String option = key.split("\\.")[1];
                if (options.containsKey(category))
                    options.get(category).add(option);
                else
                    options.put(category, Collections.singletonList(option));
            }
            EmbedBuilder eb = new EmbedBuilder().setColor(Color.magenta).setDescription(
                    "All options available for this guild");
            for (String s : options.keySet())
                eb.addField(StringUtils.capitalize(s), options.get(s).stream().map(option -> "`" + option + "`")
                        .collect(Collectors.joining("\n")), false);
            channel.sendMessage(eb.build()).queue();
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("get")) {
                if (guild.getOptions().hasOption(args[1]))
                    channel.sendMessage("`" + args[1] + "` - " + guild.getOptions().get(args[1])).queue();
                else
                    channel.sendMessage("Well that option doesn't exist does not now... *sigh* some people...").queue();
            } else if (args[0].equalsIgnoreCase("set")) {
                if (guild.getOptions().hasOption(args[1]))
                    MessageUtils.sendErrorMessage("Usage: {%}options set " + args[1] + " <value>", channel);
                else
                    MessageUtils.sendErrorMessage("Do `{%}options list` for a list of possible options", channel);
            }
        } else if (args.length == 3) {
            if(args[0].equalsIgnoreCase("set")){
                if (guild.getOptions().hasOption(args[1])) {
                    if(guild.getOptions().setOption(args[1], args[2]))
                        MessageUtils.sendSuccessMessage("Changed `" + args[1] + "` to `" + args[2] + "`", channel);
                    else
                        MessageUtils.sendErrorMessage("Failed to change the value of " + args[1] + " make sure you're " +
                                "using the correct type!", channel);
                }
                else
                    channel.sendMessage("Well that option doesn't exist does not now... *sigh* some people...").queue();
            }else{
                MessageUtils.sendErrorMessage("I've no idea what you're trying to do but maybe try something like you " +
                        "know... shown in the usage? smh", channel);
            }
        } else {
            MessageUtils.sendUsage(this, channel, sender);
        }
    }

    @Override
    public String getCommand() {
        return "options";
    }

    @Override
    public String getDescription() {
        return "Options for a guild";
    }

    @Override
    public String getUsage() {
        return "`{%}options set <option> <value>` - Set an option to a desired value\n" +
                "`{%}options get <option>` - Get an options value\n" +
                "`{%}options list` - List all available options";
    }

    @Override
    public CommandType getType() {
        return CommandType.CONFIGURATION;
    }

    @Override
    public boolean isBetaTesterCommand() {
        return true;
    }
}
