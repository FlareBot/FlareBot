package stream.flarebot.flarebot.commands;

import net.dv8tion.jda.core.entities.User;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.currency.ConvertCommand;
import stream.flarebot.flarebot.commands.currency.CurrencyCommand;
import stream.flarebot.flarebot.commands.general.*;
import stream.flarebot.flarebot.commands.informational.*;
import stream.flarebot.flarebot.commands.management.PermissionsCommand;
import stream.flarebot.flarebot.commands.management.SetPrefixCommand;
import stream.flarebot.flarebot.commands.management.WelcomeCommand;
import stream.flarebot.flarebot.commands.moderation.*;
import stream.flarebot.flarebot.commands.moderation.mod.*;
import stream.flarebot.flarebot.commands.music.*;
import stream.flarebot.flarebot.commands.random.AvatarCommand;
import stream.flarebot.flarebot.commands.secret.*;
import stream.flarebot.flarebot.commands.secret.internal.ChangelogCommand;
import stream.flarebot.flarebot.commands.secret.internal.PostUpdateCommand;
import stream.flarebot.flarebot.commands.useful.RemindCommand;
import stream.flarebot.flarebot.commands.useful.TagsCommand;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;

import java.util.Set;
import java.util.stream.Collectors;

public class CommandManager {

    private final Set<Command> commands = new ConcurrentHashSet<>();
    private final Logger LOGGER = FlareBot.getLog(CommandManager.class);

    public CommandManager() {
        int start = count();
        registerMusicCommands();
        LOGGER.info("[Command Manager] Loaded " + (count() - start) + " music commands!");

        start = count();
        registerGeneralCommands();
        LOGGER.info("[Command Manager] Loaded " + (count() - start) + " general commands!");

        start = count();
        registerModerationCommands();
        LOGGER.info("[Command Manager] Loaded " + (count() - start) + " moderation commands!");

        start = count();
        registerSecretCommands();
        LOGGER.info("[Command Manager] Loaded " + (count() - start) + " secret commands!");

        start = count();
        registerMiscCommands();
        LOGGER.info("[Command Manager] Loaded " + (count() - start) + " misc commands!");

    }

    // https://bots.are-pretty.sexy/214501.png
    // New way to process commands, this way has been proven to be quicker overall.
    public Command getCommand(String s, User user) {
        if (PerGuildPermissions.isCreator(user) || (FlareBot.instance().isTestBot() && PerGuildPermissions.isContributor(user))) {
            for (Command cmd : getCommandsByType(CommandType.SECRET)) {
                if (cmd.getCommand().equalsIgnoreCase(s))
                    return cmd;
                for (String alias : cmd.getAliases())
                    if (alias.equalsIgnoreCase(s)) return cmd;
            }
        }
        for (Command cmd : getCommands()) {
            if (cmd.getType() == CommandType.SECRET) continue;
            if (cmd.getCommand().equalsIgnoreCase(s))
                return cmd;
            for (String alias : cmd.getAliases())
                if (alias.equalsIgnoreCase(s)) return cmd;
        }
        return null;
    }

    private void registerGeneralCommands() {
        registerCommand(new HelpCommand());
        registerCommand(new InfoCommand());
        registerCommand(new InviteCommand());
        registerCommand(new SelfAssignCommand());
        registerCommand(new ServerInfoCommand());
        registerCommand(new StatusCommand());
        registerCommand(new StatsCommand());
        registerCommand(new ReportCommand());
        registerCommand(new ShardInfoCommand());
        registerCommand(new UserInfoCommand());
        registerCommand(new CommandUsageCommand());
    }

    private void registerMusicCommands() {
        registerCommand(new SongNickCommand());
        registerCommand(new SearchCommand());
        registerCommand(new JoinCommand());
        registerCommand(new LeaveCommand());
        registerCommand(new ResumeCommand());
        registerCommand(new PlayCommand());
        registerCommand(new PauseCommand());
        registerCommand(new StopCommand());
        registerCommand(new SkipCommand());
        registerCommand(new ShuffleCommand());
        registerCommand(new QueueCommand());
        registerCommand(new SongCommand());
        registerCommand(new RepeatCommand());
        registerCommand(new MusicAnnounceCommand());
        registerCommand(new LoopCommand());
        registerCommand(new LoadCommand());
        registerCommand(new SaveCommand());
        registerCommand(new DeleteCommand());
        registerCommand(new PlaylistsCommand());
        registerCommand(new SeekCommand());
    }

    private void registerModerationCommands() {
        registerCommand(new KickCommand());
        registerCommand(new WarnCommand());
        registerCommand(new WarningsCommand());
        registerCommand(new ForceBanCommand());
        registerCommand(new BanCommand());
        registerCommand(new TempBanCommand());
        registerCommand(new UnbanCommand());
        registerCommand(new MuteCommand());
        registerCommand(new TempMuteCommand());
        registerCommand(new UnmuteCommand());
        registerCommand(new LockChatCommand());
        registerCommand(new ModlogCommand());
        registerCommand(new SetPrefixCommand());
        registerCommand(new PurgeCommand());
        registerCommand(new PinCommand());
        registerCommand(new ReportsCommand());
        registerCommand(new PruneCommand());
        registerCommand(new RolesCommand());
        registerCommand(new WelcomeCommand());
        registerCommand(new PermissionsCommand());
        registerCommand(new AutoAssignCommand());
        registerCommand(new FixCommand());
        registerCommand(new NINOCommand());
    }

    private void registerSecretCommands() {
        registerCommand(new QuitCommand());
        registerCommand(new UpdateCommand());
        registerCommand(new LogsCommand());
        registerCommand(new EvalCommand());
        registerCommand(new ChangeAvatarCommand());
        registerCommand(new ShardRestartCommand());
        registerCommand(new QueryCommand());
        registerCommand(new UpdateJDACommand());
        registerCommand(new ChangelogCommand());
        registerCommand(new TestCommand());
        registerCommand(new PostUpdateCommand());
        registerCommand(new GuildCommand());
        registerCommand(new DisableCommandCommand());
        registerCommand(new DebugCommand());
    }

    private void registerMiscCommands() {
        registerCommand(new CurrencyCommand());
        registerCommand(new ConvertCommand());
        registerCommand(new TagsCommand());
        registerCommand(new RemindCommand());
        registerCommand(new AvatarCommand());
        registerCommand(new BetaCommand());
        registerCommand(new DonateCommand());
    }

    private void registerCommand(Command command) {
        commands.add(command);
    }

    public Set<Command> getCommands() {
        return commands;
    }

    public Set<Command> getCommandsByType(CommandType type) {
        return commands.stream().filter(command -> command.getType() == type).collect(Collectors.toSet());
    }

    public int count() {
        return commands.size();
    }


}
