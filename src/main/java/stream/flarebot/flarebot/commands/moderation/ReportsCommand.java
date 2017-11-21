package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.Report;
import stream.flarebot.flarebot.objects.ReportStatus;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
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
                        List<Report> reports = guild.getReportManager().getReports();
                        int page = 1;
                        final int reportsLength = 15;
                        if (args.length == 2) {
                            try {
                                page = Integer.valueOf(args[1]);
                            } catch (NumberFormatException e) {
                                MessageUtils.sendErrorMessage("Invalid page number: " + args[1] + ".", channel);
                                return;
                            }
                        }
                        int pages =
                                reports.size() < reportsLength ? 1 : (reports.size() / reportsLength) + (reports.size() % reportsLength != 0 ? 1 : 0);
                        int start;
                        int end;

                        start = reportsLength * (page - 1);
                        end = Math.min(start + reportsLength, reports.size());
                        if (page > pages || page < 0) {
                            MessageUtils.sendErrorMessage("That page doesn't exist. Current page count: " + pages, channel);
                        } else {
                            List<Report> subReports = reports.subList(start, end);

                            if (reports.isEmpty()) {
                                MessageUtils.sendInfoMessage("No Reports for this guild!", channel, sender);
                            } else {
                                channel.sendMessage(getReportsTable(subReports, " Reports Page " + GeneralUtils.getPageOutOfTotal(page, reports, reportsLength))).queue();
                            }
                        }
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
                            errorBuilder.addField("Statuses", "**" + Arrays.stream(ReportStatus.values()).map(ReportStatus::getMessage).collect(Collectors.joining("**\n**")) + "**", false);
                            MessageUtils.sendErrorMessage(errorBuilder, channel);
                            return;
                        }
                        if (guild.getReportManager().getReport(id).getStatus().equals(status)) {
                            MessageUtils.sendInfoMessage("Current status is: **" + status.getMessage() + "**", channel, sender);
                        } else {
                            ReportStatus old = guild.getReportManager().getReport(id).getStatus();
                            guild.getReportManager().getReport(id).setStatus(status);
                            MessageUtils.sendSuccessMessage(String.format("Changed status of Report with ID: **%d** to **%s**", id, status.getMessage()), channel, sender);
                            guild.getAutoModConfig().postToModLog(new EmbedBuilder()
                                    .setTitle("Report edited")
                                    .setColor(Color.WHITE)
                                    .addField("Report ID", String.valueOf(id), true)
                                    .addField("Old Status", old.getMessage(), true)
                                    .addField("New Status", guild.getReportManager().getReport(id).getStatus().getMessage(), true)
                                    .addField("Responsible moderator", sender.getAsMention(), true).build());
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

    private String getReportsTable(List<Report> reports, String footer) {
        ArrayList<String> header = new ArrayList<>();
        header.add("Id");
        header.add("Reported");
        header.add("Time");
        header.add("Status");

        List<List<String>> table = new ArrayList<>();
        for (Report report : reports) {
            ArrayList<String> row = new ArrayList<>();
            row.add(String.valueOf(report.getId()));
            row.add(MessageUtils.getTag(FlareBot.getInstance().getUserByID(String.valueOf(report.getReportedId()))));

            row.add(report.getTime().toLocalDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " GMT/BST");

            row.add(report.getStatus().getMessage());

            table.add(row);
        }

        return MessageUtils.makeAsciiTable(header, table, footer, "swift");
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
