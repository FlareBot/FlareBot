package stream.flarebot.flarebot.commands.custom;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.customcommands.CustomCommandParser;
import stream.flarebot.flarebot.objects.GuildWrapper;

// Shouldn't this technically be CustomCommandCommand?
public class CustomCommand implements Command {

    private final String CUSTOM_COMMAND = "`\\{user.id\\}` - {user.id}\n" +
            "`\\{user.name\\}` - {user.name}\n" +
            "`\\{user.nick\\}` - {user.nick}\n" +
            "`\\{user.discriminator\\}` - {user.discriminator}\n" +
            "`\\{user.tag\\}` - {user.tag}\n" +
            "`\\{user.mention\\}` - {user.mention}\n" +
            "`\\{user.game\\}` - {user.game}\n" +
            "`\\{user.gametime\\}` - {user.gametime}\n" +
            "`\\{user.avatar\\}` - {user.avatar}\n" +
            "`\\{user.createdAt\\}` - {user.createdAt}\n" +
            "`\\{user.joinedAt\\}` - {user.joinedAt}\n" +
            "`\\{server.id\\}` - {server.id}\n" +
            "`\\{server.name\\}` - {server.name}\n" +
            "`\\{server.icon\\}` - {server.icon}\n" +
            "`\\{server.memberCount\\}` - {server.memberCount}\n" +
            "`\\{server.ownerId\\}` - {server.ownerId}\n" +
            "`\\{server.createdAt\\}` - {server.createdAt}\n" +
            "`\\{server.region\\}` - {server.region}\n" +
            "`\\{channel.id\\}` - {channel.id}\n" +
            "`\\{channel.name\\}` - {channel.name}\n" +
            "`\\{channel.mention\\}` - {channel.mention}\n" +
            "`\\{channel.topic\\}` - {channel.topic}\n" +
            "`\\{prefix\\}` - {prefix}\n" +
            "`\\{command:userinfo \\{user.id\\}\\}` - {command:userinfo {user.id}}\n" +
            "`\\{choose:apples;test\\}` - {choose:apples;test}\n" +
            "`\\{choice:apples;oranges;sheldon\\}` - {choice:apples;oranges;sheldon}\n" +
            "`\\{randnum:100-1000\\}` - {randnum:100-1000}";

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        CustomCommandParser parser = new CustomCommandParser();
        channel.sendMessage(parser.parseCustomCommand("Welcome you're currently the {server.memberCount} member on the server! " +
                        "This is {channel.mention} and this is for {channel.topic}. The bot prefix is `{prefix}`.",
                sender, channel, guild)).queue();
    }

    @Override
    public String getCommand() {
        return "customcommand";
    }

    @Override
    public String getDescription() {
        return "Custom command stuff";
    }

    @Override
    public String getUsage() {
        return "`{%}customcommand` - /shrug";
    }

    @Override
    public CommandType getType() {
        return CommandType.USEFUL;
    }
}
