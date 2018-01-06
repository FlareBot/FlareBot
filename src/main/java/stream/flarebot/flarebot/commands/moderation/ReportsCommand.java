package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.Getters;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.mod.modlog.ModlogEvent;
import stream.flarebot.flarebot.mod.modlog.ModlogHandler;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.Report;
import stream.flarebot.flarebot.objects.ReportStatus;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.PaginationUtil;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReportsCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender, args);
        } else {
            if (args[0].equalsIgnoreCase("list")) {
                if (args.length <= 2) {
                    if (getPermissions(channel).hasPermission(member, "flarebot.reports.list")) {
                        ArrayList<String> header = new ArrayList<>();
                        header.add("Id");
                        header.add("Reported");
                        header.add("Time");
                        header.add("Status");
                        List<Report> reports = guild.getReportManager().getReports();

                        if (reports.isEmpty()) {
                            MessageUtils.sendInfoMessage("No Reports for this guild!", channel, sender);
                            return;
                        }

                        List<List<String>> body = getReportsTable(reports);
                        int page = 0;
                        if (args.length == 2) {
                            try {
                                page = Integer.valueOf(args[1]);
                            } catch (NumberFormatException e) {
                                MessageUtils.sendErrorMessage("Invalid page!", channel);
                                return;
                            }
                        }
                        PaginationUtil.sendPagedMessage(channel, PaginationUtil.buildPagedTable(header, body, 10), page);
                    } else {
                        MessageUtils.sendErrorMessage("You need the permission `flarebot.reports.list`", channel);
                    }
                } else {
                    MessageUtils.sendUsage(this, channel, sender, args);
                }
            } else if (args[0].equalsIgnoreCase("view")) {
                if (args.length == 2) {
                    int id;
                    try {
                        id = Integer.valueOf(args[1]);
                    } catch (Exception e) {
                        MessageUtils.sendErrorMessage("Invalid report number: " + args[1] + ".", channel);
                        return;
                    }

                    Report report = guild.getReportManager().getReport(id);
                    if (report == null) {
                        MessageUtils.sendErrorMessage("That report doesn't exist.", channel);
                        return;
                    }

                    if (getPermissions(channel).hasPermission(member, "flarebot.reports.view") || report.getReporterId().equals(sender.getId())) {
                        channel.sendMessage(GeneralUtils.getReportEmbed(sender, report).build()).queue();
                    } else {
                        MessageUtils.sendErrorMessage("You need the permission `flarebot.reports.view` to do this! Or you need to be the creator of the report", channel);
                    }
                } else {
                    MessageUtils.sendUsage(this, channel, sender, args);
                }
            } else if (args[0].equalsIgnoreCase("status")) {
                if (args.length >= 3) {
                    if (getPermissions(channel).hasPermission(member, "flarebot.report.status")) {
                        int id;
                        try {
                            id = Integer.valueOf(args[1]);
                        } catch (Exception e) {
                            MessageUtils.sendErrorMessage("Invalid report number: " + args[1] + ".", channel);
                            return;
                        }
                        ReportStatus status;
                        try {
                            status =
                                    ReportStatus.valueOf(MessageUtils.getMessage(args, 2).toUpperCase().replace(" ", "_"));
                        } catch (IllegalArgumentException e) {
                            EmbedBuilder errorBuilder = new EmbedBuilder();
                            errorBuilder.setDescription("Invalid status: `" + args[2] + "`");
                            errorBuilder.addField("Statuses", "**" + Arrays.stream(ReportStatus.values())
                                    .map(ReportStatus::getMessage).collect(Collectors.joining("**\n**")) + "**", false);
                            MessageUtils.sendErrorMessage(errorBuilder, channel);
                            return;
                        }
                        Report report = guild.getReportManager().getReport(id);
                        if (report == null) {
                            MessageUtils.sendErrorMessage("Invalid report ID!", channel);
                            return;
                        }

                        if (report.getStatus() == status) {
                            MessageUtils.sendInfoMessage("Current status is: **" + status.getMessage() + "**", channel, sender);
                        } else {
                            ReportStatus old = report.getStatus();
                            report.setStatus(status);
                            MessageUtils.sendSuccessMessage(String.format("Changed status of Report with ID: **%d** to **%s**", id, status.getMessage()), channel, sender);
                            ModlogHandler.getInstance().postToModlog(guild, ModlogEvent.REPORT_EDITED, null, sender, null,
                                    new MessageEmbed.Field("Report ID", String.valueOf(id), true),
                                    new MessageEmbed.Field("Old Status", old.getMessage(), true),
                                    new MessageEmbed.Field("New Status", report.getStatus().getMessage(), true),
                                    new MessageEmbed.Field("Responsible moderator", sender.getAsMention(), true));
                        }
                    } else {
                        MessageUtils.sendErrorMessage("You need the permission `flarebot.reports.status` to do this.", channel);
                    }
                } else {
                    MessageUtils.sendUsage(this, channel, sender, args);
                }
            } else {
                MessageUtils.sendUsage(this, channel, sender, args);
            }
        }

    }

    private List<List<String>> getReportsTable(List<Report> reports) {
        List<List<String>> table = new ArrayList<>();
        for (Report report : reports) {
            ArrayList<String> row = new ArrayList<>();
            row.add(String.valueOf(report.getId()));
            row.add(MessageUtils.getTag(Getters.getUserById(String.valueOf(report.getReportedId()))));

            row.add(report.getTime().toLocalDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " GMT/BST");

            row.add(report.getStatus().getMessage());

            table.add(row);
        }

        return table;
    }

    @Override
    public String getCommand() {
        return "reports";
    }

    @Override
    public String getDescription() {
        return "Used to view reports and to report users";
    }

    @Override
    public String getUsage() {
        return "`{%}reports` - Shows the usage.\n" +
                "`{%}reports list [page]` - List the reports on your guild.\n" +
                "`{%}reports view <number>` - Views a report with the given number.\n" +
                "`{%}reports status <number> <status>` - Edits the status of a report.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

}
