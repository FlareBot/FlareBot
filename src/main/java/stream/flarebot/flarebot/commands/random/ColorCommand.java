package stream.flarebot.flarebot.commands.random;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Permission;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorCommand implements Command {

    private static final Pattern HEX_COLOR = Pattern.compile("#?([A-Fa-f0-9]{6})");
    private static final Pattern RGB = Pattern.compile("(\\d{1,3}),(\\d{1,3}),(\\d{1,3})");

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1) {
            String hex;
            Color color = null;
            Matcher matcher;
            if ((matcher = HEX_COLOR.matcher(args[0])).find()) {
                hex = matcher.group();
                if (hex.startsWith("#"))
                    hex = hex.substring(1);
            } else if ((matcher = RGB.matcher(args[0])).find()) {
                hex = getHex(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(3)));
            } else {
                try {
                    color = (Color) Color.class.getField(args[0].toUpperCase()).get(null);
                    hex = getHex(color.getRed(), color.getBlue(), color.getGreen());
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                    MessageUtils.sendWarningMessage("Please specify a correct color either by a color name, rgb " +
                            "value or a Hex color code!", channel);
                    return;
                }
            }
            if (hex.length() > 6) {
                MessageUtils.sendWarningMessage("Invalid color code!", channel);
                return;
            }

            if (color == null)
                color = Color.decode('#' + hex);
            int unsignedInt = Integer.parseUnsignedInt(hex, 16);
            EmbedBuilder eb = MessageUtils.getEmbed().setColor(color).setTitle("#" + hex)
                    .addField("RGB", color.getRed() + ", " + color.getGreen() + ", " + color.getBlue(), true)
                    .addField("Numbers", String.format("Binary: %s\nDecimal: %d", Integer.toBinaryString(unsignedInt),
                            unsignedInt), true)
                    .setThumbnail("https://api.flarebot.stream/image.png?color=" + hex);
            channel.sendMessage(eb.build()).queue();
        } else {
            channel.sendMessage("Send me a color name, rgb value or hex code for all the infos!").queue();
        }
    }

    private String getHex(int r, int g, int b) {
        return String.format("%02x%02x%02x", r, g, b);
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
    public Permission getPermission() {
        return Permission.COLOR_COMMAND;
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
