package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.Report;
import stream.flarebot.flarebot.objects.ReportStatus;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.ReportManager;

import java.awt.*;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReportsCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.getUsage(this, channel).queue();
        } else {
            if (args[0].equalsIgnoreCase("list")) {
                if (args.length <= 2) {
                    if (getPermissions(message.getChannel()).hasPermission(member, "flarebot.reports.list")) {
                        List<Report> reports = ReportManager.getInstance().getGuildReports(channel.getGuild().getId());
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
                        int pages = reports.size() < reportsLength ? 1 : (reports.size() / reportsLength) + (reports.size() % reportsLength != 0 ? 1 : 0);
                        int start;
                        int end;

                        start = reportsLength * (page - 1);
                        end = Math.min(start + reportsLength, reports.size());
                        if (page > pages || page < 0) {
                            MessageUtils.sendErrorMessage("That page doesn't exist. Current page count: " + pages, channel);
                        } else {
                            List<Report> subReports = reports.subList(start, end);

                            if (reports.isEmpty()) {
                                channel.sendMessage(MessageUtils.getEmbed(sender).setColor(Color.CYAN).setDescription("No Reports for this guild!").build()).queue();
                            } else {
                                channel.sendMessage(getReportsTable(subReports, " Reports Page " + getPageOutOfTotal(page, reports, reportsLength))).queue();
                            }
                        }
                    } else {
                        MessageUtils.sendErrorMessage("You need the permission `flarebot.reports.list`", channel);
                    }
                } else {
                    MessageUtils.getUsage(this, channel);
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

                    Report report = ReportManager.getInstance().getReport(channel.getGuild().getId(), id);
                    if (report == null) {
                        MessageUtils.sendErrorMessage("That report doesn't exist.", channel);
                        return;
                    }

                    if (getPermissions(message.getChannel()).hasPermission(member, "flarebot.reports.view") || report.getReporterId().equals(sender.getId())) {
                        channel.sendMessage(MessageUtils.getReportEmbed(sender, report, channel).build()).queue();
                    } else {
                        MessageUtils.sendErrorMessage("You need the permission `flarebot.reports.view` to do this! Or you need to be the creator of the report", channel);
                    }
                } else {
                    MessageUtils.getUsage(this, channel).queue();
                }
            } else if (args[0].equalsIgnoreCase("status")) {
                if (args.length >= 3) {
                    if (getPermissions(message.getChannel()).hasPermission(member, "flarebot.report.status")) {
                        int id;
                        try {
                            id = Integer.valueOf(args[1]);
                        } catch (Exception e) {
                            MessageUtils.sendErrorMessage("Invalid report number: " + args[1] + ".", channel);
                            return;
                        }
                        ReportStatus status;
                        try {
                            status = ReportStatus.valueOf(MessageUtils.getMessage(args, 2).toUpperCase().replace(" ", "_"));
                        } catch (IllegalArgumentException e) {
                            EmbedBuilder errorBuilder = new EmbedBuilder();
                            errorBuilder.setDescription("Invalid status: `" + args[2] + "`");
                            errorBuilder.addField("Statuses", "**" + Arrays.stream(ReportStatus.values()).map(ReportStatus::getMessage).collect(Collectors.joining("**\n**")) + "**", false);
                            MessageUtils.sendErrorMessage(errorBuilder, channel);
                            return;
                        }
                        if (ReportManager.getInstance().getReport(channel.getGuild().getId(), id).getStatus().equals(status)) {
                            channel.sendMessage(MessageUtils.getEmbed(sender).setColor(Color.CYAN).setDescription("Current status is: **" + status.getMessage() + "**").build()).queue();
                        } else {
                            ReportManager.getInstance().getReport(channel.getGuild().getId(), id).setStatus(status);
                            channel.sendMessage(MessageUtils.getEmbed(sender).setColor(Color.GREEN).setDescription(String.format("Changed status of Report with ID: **%d** to **%s**", id, status.getMessage())).build()).queue();

                        }
                    } else {
                        MessageUtils.sendErrorMessage("You need the permission `flarebot.reports.status` to do this.", channel);
                    }
                } else {
                    MessageUtils.getUsage(this, channel).queue();
                }
            } else {
                MessageUtils.getUsage(this, channel).queue();
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

            row.add(report.getTime().toLocalDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

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
        return "`{%}reports` - shows the usage\n" +
                "`{%}reports list [page]` - list the reports on your guild\n" +
                "`{%}reports view <number>` - views a report with the given number\n" +
                "`{%}reports status <number> <status>` - edits the status of a report\n";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    private String getPageOutOfTotal(int page, List<Report> reports, int reportsSize) {
        return String.valueOf(page) + "/" + String.valueOf(reports.size() < reportsSize ? 1 : (reports.size() / reportsSize) + (reports.size() % reportsSize != 0 ? 1 : 0));
    }

}
