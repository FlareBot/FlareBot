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

   private static final Pattern hex_color = Pattern.compile("^#?([A-Fa-f0-9]{6})$");
    
    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1) {
            String hex;
            Color color;
            Matcher matcher = hex_color.matcher(args[0]);
            if (matcher.find()) {
                hex = matcher.group();
                color = Color.decode(hex);
            } else {
                try {
                    color = (Color) Color.class.getField(args[0].toUpperCase()).get(null);
                    hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                    MessageUtils.sendErrorMessage("Please specify a correct color either by a Java color name or a Hex color code!", channel);
                    return;
                }
            }
            EmbedBuilder eb = MessageUtils.getEmbed().setColor(color).setTitle(hex)
                    .addField("RGB", "`" + color.getRed() + "`, `" + color.getGreen() + "`, `" + color.getBlue() + "`", false);
            channel.sendMessage(eb.build()).queue();
        } else {

        }
    }

    @Override
    public String getCommand() {
        return "color";
    }

    @Override
    public String getDescription() {
        return "Allows getting information of a color";
    }

    @Override
    public String getUsage() {
        return "`{%}color <color>` - Gets information about a color";
    }

    @Override
    public CommandType getType() {
        return CommandType.RANDOM;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"colour"};
    }
}
