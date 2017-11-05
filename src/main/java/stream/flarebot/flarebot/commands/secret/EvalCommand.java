package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Markers;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class EvalCommand implements Command {

    private ScriptEngineManager manager = new ScriptEngineManager();
    private static final ThreadGroup EVALS = new ThreadGroup("EvalCommand Thread Pool");
    private static final ExecutorService POOL = Executors.newCachedThreadPool(r -> new Thread(EVALS, r,
            EVALS.getName() + EVALS.activeCount()));
    private static final List<String> IMPORTS = Arrays.asList("stream.flarebot.flarebot",
            "stream.flarebot.flarebot.music",
            "stream.flarebot.flarebot.util",
            "stream.flarebot.flarebot.mod",
            "stream.flarebot.flarebot.mod.events",
            "stream.flarebot.flarebot.scheduler",
            "stream.flarebot.flarebot.permissions",
            "stream.flarebot.flarebot.commands",
            "stream.flarebot.flarebot.music.extractors",
            "stream.flarebot.flarebot.util.currency",
            "stream.flarebot.flarebot.util.objects",
            "net.dv8tion.jda.core",
            "net.dv8tion.jda.core.managers",
            "net.dv8tion.jda.core.entities.impl",
            "net.dv8tion.jda.core.entities",
            "net.dv8tion.jda.core.utils",
            "java.util.streams",
            "java.util",
            "java.lang",
            "java.text",
            "java.lang",
            "java.math",
            "java.time",
            "java.io",
            "java.nio",
            "java.nio.files",
            "java.util.stream");

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        String imports =
                IMPORTS.stream().map(s -> "Packages." + s).collect(Collectors.joining(", ", "var imports = new JavaImporter(", ");\n"));
        ScriptEngine engine = manager.getEngineByName("nashorn");
        engine.put("channel", channel);
        engine.put("guild", guild);
        engine.put("message", message);
        engine.put("jda", sender.getJDA());
        engine.put("sender", sender);
        String code;
        boolean silent = args.length > 0 && args[0].equalsIgnoreCase("-s");
        if (silent)
            code = FlareBot.getMessage(args, 1);
        else
            code = Arrays.stream(args).collect(Collectors.joining(" "));
        POOL.submit(() -> {
            try {
                String eResult = String.valueOf(engine.eval(imports + "with (imports) {\n" + code + "\n}"));
                if (("```js\n" + eResult + "\n```").length() > 1048) {
                    eResult = String.format("[Result](%s)", MessageUtils.paste(eResult));
                } else eResult = "```js\n" + eResult + "\n```";
                if (!silent)
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .addField("Code:", "```js\n" + code + "```", false)
                            .addField("Result: ", eResult, false).build()).queue();
            } catch (Exception e) {
                FlareBot.LOGGER.error("Error occured in the evaluator thread pool!", e, Markers.NO_ANNOUNCE);
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .addField("Code:", "```js\n" + code + "```", false)
                        .addField("Result: ", "```bf\n" + e.getMessage() + "```", false).build()).queue();
            }
        });
    }

    @Override
    public String getCommand() {
        return "eval";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public CommandType getType() {
        return CommandType.SECRET;
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }
}