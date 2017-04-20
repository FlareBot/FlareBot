package stream.flarebot.flarebot.commands.secret;

import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.core.entities.*;

public class AvatarCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            if (!message.getAttachments().isEmpty()) {
                Message.Attachment attachment = message.getAttachments().get(0);
                try {
                    sender.getJDA().getSelfUser().getManager().setAvatar(Icon.from(
                            Unirest.get(attachment.getUrl()).header("User-Agent", "Mozilla/5.0 FlareBot").asBinary().getBody()
                    )).complete();
                } catch (Exception e) {
                    channel.sendMessage("Failed to update avatar!! " + e).queue();
                    return;
                }
                channel.sendMessage("Success!").queue();
            } else {
                channel.sendMessage("You must either attach an image or link to one!").queue();
            }
        } else {
            try {
                sender.getJDA().getSelfUser().getManager().setAvatar(Icon.from(
                        Unirest.get(args[0]).header("User-Agent", "Mozilla/5.0 FlareBot").asBinary().getBody()
                )).complete();
            } catch (Exception e) {
                channel.sendMessage("Failed to update avatar!! " + e).queue();
                return;
            }
            channel.sendMessage("Success!").queue();
        }
    }

    @Override
    public String getCommand() {
        return "avatar";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public CommandType getType() {
        return CommandType.HIDDEN;
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }
}
