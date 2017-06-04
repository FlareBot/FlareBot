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

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportsCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel).queue();
        } else {
            if (args[0].equalsIgnoreCase("list")) {
                if (args.length <= 2) {
                    if (getPermissions(message.getChannel()).hasPermission(member, "flarebot.reports.list")) {
                        List<Report> reports = ReportManager.getInstance().getGuildReports(channel.getGuild().getId());
                        if (reports.size() > 20) {
                            if (args.length != 2) {
                                Report[] reportArray = new Report[reports.size()];
                                reportArray = reports.toArray(reportArray);
                                reportArray = Arrays.copyOfRange(reportArray, 0, 20);
                                reports = Arrays.asList(reportArray);

                                channel.sendMessage(getReportsTable(channel.getGuild(), reports)).queue();
                            } else {
                                int pages = (reports.size() / 20) + 1;
                                int page;
                                int start;
                                int end;
                                try {
                                    page = Integer.valueOf(args[1]);
                                    start = 20 * (page - 1);
                                    end = (20 * page);
                                } catch (NumberFormatException e) {
                                    MessageUtils.sendErrorMessage("Invalid page number: " + args[1] + ".", channel);
                                    return;
                                }
                                if (page > pages || page < 0) {
                                    MessageUtils.sendErrorMessage("That page doesn't exist. Current page count: " + pages, channel);
                                } else {
                                    Report[] reportArray = new Report[reports.size()];
                                    reportArray = reports.toArray(reportArray);
                                    reportArray = Arrays.copyOfRange(reportArray, start, end);
                                    reports = Arrays.asList(reportArray);

                                    channel.sendMessage(getReportsTable(channel.getGuild(), reports)).queue();
                                }
                            }
                        } else {
                            channel.sendMessage(getReportsTable(channel.getGuild(), reports)).queue();
                        }
                    }
                } else {
                    MessageUtils.sendUsage(this, channel);
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
                        MessageUtils.sendErrorMessage("You need the permission `flarebot.reports.view` to do this. Or you need to be the creator of the report", channel);
                    }
                } else {
                    MessageUtils.sendUsage(this, channel).queue();
                }
            } else if (args[0].equalsIgnoreCase("status")) {
                if (args.length <= 3) {
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
                            status = ReportStatus.valueOf(args[2].toUpperCase());
                        } catch (IllegalArgumentException e) {
                            EmbedBuilder errorBuilder = new EmbedBuilder();
                            errorBuilder.setDescription("Invalid status: " + args[2]);
                            StringBuilder sb = new StringBuilder();
                            for (ReportStatus listStatus : ReportStatus.values()) {
                                sb.append(listStatus.getMessage()).append("\n");
                            }
                            errorBuilder.addField("Statuses", "```\n" + sb.toString() + "```", false);
                            MessageUtils.sendErrorMessage(errorBuilder, channel);
                            return;
                        }
                        ReportManager.getInstance().report(channel.getGuild().getId(), ReportManager.getInstance().getReport(channel.getGuild().getId(), id).setStatus(status));
                    } else {
                        MessageUtils.sendErrorMessage("You need the permission `flarebot.reports.status` to do this.", channel);
                    }
                }
            } else {
                MessageUtils.sendUsage(this, channel);
            }
        }
    }

    private String getReportsTable(Guild guild, List<Report> reports) {
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

        return MessageUtils.makeAsciiTable(header, table, null, "swift");
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
}
