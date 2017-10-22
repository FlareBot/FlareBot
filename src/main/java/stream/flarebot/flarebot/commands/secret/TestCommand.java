package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.Lang;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;

public class TestCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if(args.length != 1) {
            channel.sendMessage("Current lang: " + guild.getL().getLangCode() + ", do `_test <language/langcode>` to change").queue();
        }else{
            Lang.Locale locale = Lang.Locale.getLang(args[0]);
            if(locale == null) {
                channel.sendMessage("Invalid noob!").queue();
                return;
            }
            guild.setL(locale);
            channel.sendMessage("Changed lang to **" + locale.getLangCode() + "**").queue();
        }
    }

    @Override
    public String getCommand() {
        return "test";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getUsage() {
        return "{%}test";
    }

    @Override
    public CommandType getType() {
        return CommandType.SECRET;
    }
}
