package stream.flarebot.flarebot.commands.general;

import com.sun.org.apache.regexp.internal.RE;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.Report;
import stream.flarebot.flarebot.objects.ReportStatus;
import stream.flarebot.flarebot.util.ReportManager;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportsCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel);
        } else if (args.length == 1 || args.length == 2) {
            switch (args[0]) { //I'm used to using switch statements. If you want this as an if statement just tell me.
                case "list": {
                    if (getPermissions(message.getChannel()).hasPermission(member, "flarebot.reports.list")) {
                        List<Report> reports = ReportManager.getGuildReports(channel.getGuild().getId());
                        if (reports.size() > 20) {
                            if (args.length != 2) {
                                int pages = (reports.size() / 20) + 1;
                                Report[] reportArray = new Report[reports.size() - 1];
                                reportArray = reports.toArray(reportArray);
                                reportArray = Arrays.copyOfRange(reportArray, 0, 19);
                                reports = Arrays.asList(reportArray);

                                String reportsTable = getReportsTable(channel.getGuild(), reports);
                                EmbedBuilder builder = MessageUtils.getEmbed(sender);
                                builder.addField("Reports", reportsTable, false);
                                builder.addField("Pages", String.valueOf(pages), true);
                                builder.addField("Current page", String.valueOf(1), true);
                                channel.sendMessage(builder.build());
                            } else {
                                int pages = (reports.size() / 20) + 1;
                                int page;
                                int start;
                                int end;
                                try {
                                    page = Integer.valueOf(args[1]);
                                    start = 20 * (page - 1);
                                    end = (20 * page) - 1;
                                } catch (Exception e) {
                                    MessageUtils.sendErrorMessage(new EmbedBuilder().setDescription("Invalid page number: " + args[1] + "."), channel);
                                    return;
                                }

                                if (page > pages || page < 0) {
                                    MessageUtils.sendErrorMessage(new EmbedBuilder().setDescription("That page doesn't exist. Current page count: " + pages), channel);
                                } else {
                                    Report[] reportArray = new Report[reports.size() - 1];
                                    reportArray = reports.toArray(reportArray);
                                    reportArray = Arrays.copyOfRange(reportArray, start, end);
                                    reports = Arrays.asList(reportArray);

                                    EmbedBuilder builder = MessageUtils.getEmbed(sender);
                                    builder.addField("Reports", getReportsTable(channel.getGuild(), reports), false);
                                    builder.addField("Pages", String.valueOf(pages), true);
                                    builder.addField("Current page", String.valueOf(page), true);
                                    channel.sendMessage(builder.build());
                                }
                            }
                        } else {
                            EmbedBuilder builder = MessageUtils.getEmbed(sender);
                            builder.addField("Reports", getReportsTable(channel.getGuild(), reports), false);
                            channel.sendMessage(builder.build());
                        }
                    } else {
                        MessageUtils.sendErrorMessage(new EmbedBuilder().setDescription("You need the permission `flarebot.reports.list` to do this."), channel);
                    }
                }
                break;
                case "view": {
                    if (args.length == 2) {
                        int id;
                        try {
                            id = Integer.valueOf(args[1]);
                        } catch (Exception e) {
                            MessageUtils.sendErrorMessage(new EmbedBuilder().setDescription("Invalid report number: " + args[1] + "."), channel);
                            return;
                        }

                        Report report = ReportManager.getReport(channel.getGuild().getId(), id);
                        if (report == null) {
                            MessageUtils.sendErrorMessage(new EmbedBuilder().setDescription("That report doesn't exist."), channel);
                            return;
                        }

                        if (getPermissions(message.getChannel()).hasPermission(member, "flarebot.reports.view") || report.getReporterId() == sender.getId()) {
                            channel.sendMessage(getReportEmbed(sender, report, channel).build());
                        } else {
                            MessageUtils.sendErrorMessage(new EmbedBuilder().setDescription("You need the permission `flarebot.reports.view` to do this. Or you need to be the creator of the report"), channel);
                        }
                    } else {
                        MessageUtils.sendUsage(this, channel);
                    }

                }
                break;
                default: {
                    MessageUtils.sendUsage(this, channel);
                }
            }
        } else if (args.length >= 4) {
            switch (args[0]) {
                case "status": {
                    if (getPermissions(message.getChannel()).hasPermission(member, "flarebot.report.status")) {
                        int id;
                        try {
                            id = Integer.valueOf(args[1]);
                        } catch (Exception e) {
                            MessageUtils.sendErrorMessage(new EmbedBuilder().setDescription("Invalid report number: " + args[1] + "."), channel);
                            return;
                        }
                        ReportStatus status = ReportStatus.valueOf(args[2].toUpperCase());
                        if (status == null) {
                            EmbedBuilder errorBuilder = new EmbedBuilder();
                            errorBuilder.setDescription("Invalid status: " + args[2]);
                            errorBuilder.addField("Statuses", "```\nOPEN\nON_HOLD\nRESOLVED\nCLOSED\nUNDER_REVIEW\nDUPLICATE\n```", false);
                            MessageUtils.sendErrorMessage(errorBuilder, channel);
                            return;
                        }

                        ReportManager.reportsToSave.add(ReportManager.getReport(channel.getGuild().getId(), id).setStatus(status));
                    } else {
                        MessageUtils.sendErrorMessage(new EmbedBuilder().setDescription("You need the permission `flarebot.reports.status` to do this."), channel);
                    }
                }
                break;
                case "report": {
                    User user = MessageUtils.getUser(args[1]);
                    if (user == null) {
                        MessageUtils.sendErrorMessage("Invalid user: " + args[1], channel);
                        return;
                    }

                    String reason = MessageUtils.getMessage(args, 2);

                    Report report = new Report(channel.getGuild().getId(), 0, reason, sender.getId(), user.getId(), new Timestamp(System.currentTimeMillis()), ReportStatus.OPEN);

                    ReportManager.reportsToSave.add(report);

                    MessageUtils.sendPM(channel, sender, getReportEmbed(sender, report, channel).setDescription("Successfully reported the user"));
                }
                break;
                default: {
                    MessageUtils.sendUsage(this, channel);
                }
            }
        } else {
            MessageUtils.sendUsage(this, channel);
        }
    }

    private String getReportsTable(Guild guild, List<Report> reports) {
        ArrayList<String> header = new ArrayList<>();
        header.add("Id");
        header.add("Reporter");
        header.add("Reported");
        header.add("Time");
        header.add("Status");

        List<List<String>> table = new ArrayList<>();
        for (Report report : reports) {
            ArrayList<String> row = new ArrayList<>();
            row.add(String.valueOf(report.getId()));
            row.add(MessageUtils.getTag(FlareBot.getInstance().getUserByID(String.valueOf(report.getReporterId()))));
            row.add(MessageUtils.getTag(FlareBot.getInstance().getUserByID(String.valueOf(report.getReportedId()))));

            LocalDateTime date = report.getTime().toLocalDateTime();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

            row.add(date.format(formatter));

            row.add(report.getStatus().getMessage(guild.getId()));

            table.add(row);
        }

        return MessageUtils.makeAsciiTable(header, table, null);
    }

    public EmbedBuilder getReportEmbed(User sender, Report report, TextChannel channel){
        EmbedBuilder eb = MessageUtils.getEmbed(sender);
        User reporter = FlareBot.getInstance().getUserByID(String.valueOf(report.getReporterId()));
        User reported = FlareBot.getInstance().getUserByID(String.valueOf(report.getReportedId()));

        eb.addField("Reporter", MessageUtils.getTag(reporter), true);
        eb.addField("Reported", MessageUtils.getTag(reported), true);

        DateFormat formatedDate = new SimpleDateFormat("MM/dd/yyyy HH:mm"); //US format
        String date = formatedDate.format(report.getTime());

        eb.addField("Time", date, true);
        eb.addField("Status", report.getStatus().getMessage(channel.getGuild().getId()), true);

        eb.addField("Message", "```" + report.getMessage() + "```", false);
        return eb;
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
                "`{%}reports status <number> <status>` - edits the status of a report\n" +
                "`{%}reports report <user> [reason]` - reports a user the your guild moderators";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}
