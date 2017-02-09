package com.bwfcwalshy.flarebot.commands.secret;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Eval implements Command {
    private ScriptEngineManager manager = new ScriptEngineManager();
    private static final ThreadGroup EVALS = new ThreadGroup("Eval Thread Pool");
    private static final ExecutorService POOL = Executors.newCachedThreadPool(r -> new Thread(EVALS, r,
            EVALS.getName() + EVALS.activeCount()));
    private static final List<String> IMPORTS = Arrays.asList("com.bwfcwalshy.flarebot.*",
            "com.bwfcwalshy.flarebot.music.*",
            "com.bwfcwalshy.flarebot.util.*",
            "com.bwfcwalshy.flarebot.sheduler.*",
            "com.bwfcwalshy.flarebot.permissions.*",
            "com.bwfcwalshy.flarebot.commands.*",
            "com.bwfcwalshy.flarebot.music.extractors.*",
            "net.dv8tion.jda.core.*",
            "net.dv8tion.jda.core.managers.*",
            "net.dv8tion.jda.core.entities.impl.*",
            "net.dv8tion.jda.core.entities.*",
            "java.util.streams.*",
            "java.util.*",
            "java.text.*",
            "java.math.*",
            "java.time.*",
            "java.io.*",
            "java.nio.*",
            "java.nio.files.*");

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (getPermissions(channel).isCreator(sender)) {
            String imports = IMPORTS.stream().map(s -> "import " + s + ';').collect(Collectors.joining("\n"));
            ScriptEngine engine = manager.getEngineByName("groovy");
            engine.put("channel", channel);
            engine.put("guild", channel.getGuild());
            engine.put("message", message);
            engine.put("jda", sender.getJDA());
            engine.put("sender", sender);
            String code = Arrays.stream(args).collect(Collectors.joining(" "));
            POOL.submit(() -> {
                try {
                    String eResult = String.valueOf(engine.eval(imports + code));
                    if (("```groovy\n" + eResult + "\n```").length() > 1048) {
                        eResult = String.format("[Result](%s)", MessageUtils.hastebin(eResult));
                    } else eResult = "```groovy\n" + eResult + "\n```";
                    MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                            .addField("Code:", "```groovy\n" + code + "```", false)
                            .addField("Result: ", eResult, false), channel);
                } catch (ScriptException e) {
                    MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                            .addField("Code:", "```groovy\n" + code + "```", false)
                            .addField("Result: ", "```groovy\n" + e.getMessage() + "```", false), channel);
                } catch (Exception e) {
                    FlareBot.LOGGER.error("Error occured in the evaluator thread pool!", e);
                }
            });
        } else {
            message.addReaction("\u274C").queue();
        }
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
    public CommandType getType() {
        return CommandType.HIDDEN;
    }
}
