package stream.flarebot.flarebot.commands.random;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

public class ColourCommand implements Command {


    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1) {
            Color colour;
            if (args[0].matches("^#([A-Fa-f0-9]{6})$")) {
                colour = Color.decode(args[0]);
            } else {
                try {
                    colour = (Color) Color.class.getField(args[0].toUpperCase()).get(null);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                    MessageUtils.sendErrorMessage("Please specify a correct colour either by a Java colour name or a Hex colour code!", channel);
                    return;
                }
            }
            EmbedBuilder eb = MessageUtils.getEmbed();
            eb.setColor(colour);
            eb.addField("RGB", "`" + colour.getRed() + "`, `" + colour.getGreen() + "`, `" + colour.getBlue() + "`", false);
            channel.sendMessage(eb.build()).queue();
        } else {

        }
    }

    @Override
    public String getCommand() {
        return "colour";
    }

    @Override
    public String getDescription() {
        return "Allows getting information of a colour";
    }

    @Override
    public String getUsage() {
        return "`{%}colour <colour>` - Gets information about a colour";
    }

    @Override
    public CommandType getType() {
        return CommandType.RANDOM;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"color"};
    }
}
