package stream.flarebot.flarebot.commands.general;

import com.sun.org.apache.regexp.internal.RE;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.Report;
import stream.flarebot.flarebot.util.ReportManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

                                    String reportsTable = getReportsTable(channel.getGuild(), reports);
                                    EmbedBuilder builder = MessageUtils.getEmbed(sender);
                                    builder.addField("Reports", reportsTable, false);
                                    builder.addField("Pages", String.valueOf(pages), true);
                                    builder.addField("Current page", String.valueOf(page), true);
                                    channel.sendMessage(builder.build());
                                }
                            }
                        } else {
                            String reportsTable = getReportsTable(channel.getGuild(), reports);
                            EmbedBuilder builder = MessageUtils.getEmbed(sender);
                            builder.addField("Reports", reportsTable, false);
                            channel.sendMessage(builder.build());
                        }
                    } else {
                        MessageUtils.sendErrorMessage(new EmbedBuilder().setDescription("You need the permission `flarebot.reports.list` to do this."), channel);
                    }
                }
                break;
                default: {
                    MessageUtils.sendUsage(this, channel);
                }
            }
        } else if (args.length == 4) {
            switch (args[0]) {
                case "view": {
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
                    if(getPermissions(message.getChannel()).hasPermission(member, "flarebot.reports.view") ||
                           sender.getId() == report.getReporterId() ) {
                        EmbedBuilder eb = MessageUtils.getEmbed(sender);
                        User reporter = FlareBot.getInstance().getUserByID(String.valueOf(report.getReporterId()));
                        User reported = FlareBot.getInstance().getUserByID(String.valueOf(report.getReportedId()));

                        eb.addField("Reporter", MessageUtils.getTag(reporter), true);
                        eb.addField("Reported", MessageUtils.getTag(reported), true);

                        DateFormat formatedDate = new SimpleDateFormat("MM/dd/yyyy HH:mm"); //US format
                        String date = formatedDate.format(report.getTime());

                        eb.addField("Time", date, true);
                        eb.addField("Message", "```" + report.getMessage() + "```", false);
                    } else {
                        MessageUtils.sendErrorMessage(new EmbedBuilder().setDescription("You don't have permisons to view reports that arn't yours."), channel);
                    }
                }
                break;
                case "report": {
                    User reported = MessageUtils.getUser(MessageUtils.getMessage(args, 1));

                }
                break;
                default: {
                    MessageUtils.sendUsage(this, channel);
                }
            }
        }
    }

    private String getReportsTable(Guild guild, List<Report> reports) {
        ArrayList<String> header = new ArrayList<>();
        header.add("Id");
        header.add("Reporter");
        header.add("Reported");
        header.add("Time");
        header.add("Solved");

        List<List<String>> table = new ArrayList<>();
        for (Report report : reports) {
            ArrayList<String> row = new ArrayList<>();
            row.add(String.valueOf(report.getId()));
            row.add(MessageUtils.getTag(FlareBot.getInstance().getUserByID(String.valueOf(report.getReporterId()))));
            row.add(MessageUtils.getTag(FlareBot.getInstance().getUserByID(String.valueOf(report.getReportedId()))));

            DateFormat formatedDate = new SimpleDateFormat("MM/dd/yyyy HH:mm"); //US format
            String date = formatedDate.format(report.getTime());

            row.add(date);

            row.add(report.getStatus().getMessage(guild.getId()));

            table.add(row);
        }

        return MessageUtils.makeAsciiTable(header, table, null);
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
        return "{%}reports\n" +
                "{%}reports list [page]" +
                "{%}reports view <number>\n" +
                "{%}reports status <number> <status>\n" +
                "{%}reports report <user> [reason]";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}
