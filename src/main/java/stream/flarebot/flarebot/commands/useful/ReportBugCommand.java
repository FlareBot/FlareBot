package stream.flarebot.flarebot.commands.useful;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.GitHandler;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Permission;
import stream.flarebot.flarebot.util.ShardUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ReportBugCommand implements Command {

    private static final String ISSUE_TEMPLATE = "<!-- Issue tracker is **ONLY** used for reporting bugs. " +
            "NO FEATURE REQUESTS HERE! Use [the official guild](https://discordapp.com/invite/TTAUGvZ) " +
            "for feature requests and suggestions. -->\n" +
            "\n" +
            "## Summary of the issue\n" +
            "<!--- Provide a general summary of the issue in the Title above -->\n" +
            "%s" +
            "\n" +
            "## Expected Behavior\n" +
            "<!--- Tell us what should happen -->\n" +
            "\n" +
            "## Current Behavior\n" +
            "<!--- Tell us what happens instead of the expected behavior -->\n" +
            "\n" +
            "## Possible Solution\n" +
            "<!--- Not obligatory, but suggest a fix/reason for the bug, -->\n" +
            "\n" +
            "## Steps to Reproduce\n" +
            "<!--- Provide a link to a live example, or an unambiguous set of steps to -->\n" +
            "<!--- reproduce this bug. Include code to reproduce, if relevant -->\n" +
            "1.\n" +
            "2.\n" +
            "3.\n" +
            "4.\n" +
            "\n" +
            "## Context (Environment)\n" +
            "%s";

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (!message.getContentDisplay().contains("|")) {
            channel.sendMessage("Please include a pipe (|) to separate the title and summary!").queue();
            return;
        }
        if (args.length < 10) {
            channel.sendMessage("Please include the title and a small summary of the issue!").queue();
            return;
        }
        StringBuilder title = new StringBuilder();
        StringBuilder summary = new StringBuilder();
        boolean foundPipe = false;
        for (String s : args) {
            if (!foundPipe)
                title.append(s);
            else
                summary.append(s);
            if (s.endsWith("|") || s.equals("|"))
                foundPipe = true;
        }
        if (title.toString().isEmpty()) {
            channel.sendMessage("Please include a title!").queue();
        } else if (summary.toString().isEmpty()) {
            channel.sendMessage("Please include a summary!").queue();
        }

        try {
            String context = String.format("Shard ID: %s\n" +
                            "Version: %s\n" +
                            "Guild ID & User ID: %s",
                    ShardUtils.getShardId(guild.getGuild().getJDA()),
                    FlareBot.getVersion() + " (" + GitHandler.getLatestCommitId() + ")",
                    guild.getGuildId() + ", " + sender.getIdLong()
            );
            String url = "https://github.com/FlareBot/FlareBot/issues/new?title="
                    + URLEncoder.encode(title.toString(), "UTF-8")
                    + "&body=" + URLEncoder.encode(
                    String.format(ISSUE_TEMPLATE, summary.toString(), context)
                    , "UTF-8");

            channel.sendMessage(sender.getAsMention() + " please click the link to open your report!\n" + url).queue();
        } catch (UnsupportedEncodingException e) {
            FlareBot.LOGGER.error("Failed to encode URL", e);
            channel.sendMessage("Some weird error occurred! Err, try again please").queue();
        }

    }

    @Override
    public String getCommand() {
        return "reportbug";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"bug", "bugs", "reportb", "issue"};
    }

    @Override
    public String getDescription() {
        return "Report a bug!";
    }

    @Override
    public String getUsage() {
        return "`{%}report <Title> | <Summary of the issue>";
    }

    @Override
    public Permission getPermission() {
        return null;
    }

    @Override
    public CommandType getType() {
        return CommandType.USEFUL;
    }
}
