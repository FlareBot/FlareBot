package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
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
import java.util.Date;
import java.util.List;

public class ReportsCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        ReportManager man = new ReportManager();
        if(args.length == 0){
            MessageUtils.sendUsage(this, channel);
        } else if(args.length == 1 || args.length == 2){
            switch (args[0]){
                case "list":{
                    if(getPermissions(message.getChannel()).hasPermission(member, "flarebot.reports.list")){
                        List<Report> reports = man.getGuildReports(channel.getGuild().getIdLong());
                        if(reports.size() > 20){
                            if(args.length != 2){
                                int pages = (reports.size() / 20) + 1;
                                Report[] reportarray = new Report[reports.size() - 1];
                                reportarray = reports.toArray(reportarray);
                                reportarray = Arrays.copyOfRange(reportarray, 0, 19);
                                reports = Arrays.asList(reportarray);

                                List<String> footer = new ArrayList<String>();
                                footer.add("Pages");
                                footer.add("Current page: " + 1);
                                footer.add("Total pages" + pages);
                                footer.add("");

                                String reportsTable = getReportsTable(reports, footer);
                            } else {
                                int pages = (reports.size() / 20) + 1;
                                int page = 0;
                                int start = 0;
                                int end = 0;
                                try {
                                    page = Integer.valueOf(args[1]);
                                    start = 20 * (page - 1);
                                    end = (20 * page) - 1;
                                } catch (Exception e){
                                    MessageUtils.sendErrorMessage(new EmbedBuilder().setDescription("Invalid page number: " + args[1] + "."), channel);
                                    return;
                                }

                                if(page > pages || page < 0){
                                    MessageUtils.sendErrorMessage(new EmbedBuilder().setDescription("That page doesn't exist. Current page count: " + pages), channel);
                                } else {
                                    Report[] reportarray = new Report[reports.size() - 1];
                                    reportarray = reports.toArray(reportarray);
                                    reportarray = Arrays.copyOfRange(reportarray, start, end);
                                    reports = Arrays.asList(reportarray);

                                    List<String> footer = new ArrayList<String>();
                                    footer.add("Pages");
                                    footer.add("Current page: " + page);
                                    footer.add("Total pages" + pages);
                                    footer.add("");
                                    String reportsTable = getReportsTable(reports, footer);
                                }
                            }
                        } else {
                            String reportsTable = getReportsTable(reports, null);
                        }
                    } else {
                        MessageUtils.sendErrorMessage(new EmbedBuilder().setDescription("You need the permission `flarebot.reports.list` to do this."), channel);
                    }
                }
            }
        }
    }

    public String getReportsTable(List<Report> reports, List<String> footer){
        ArrayList<String> header = new ArrayList<String>();
        header.add("Id");
        header.add("Reporter");
        header.add("Reported");
        header.add("Time");

        List<List<String>> table = new ArrayList<>();
        for (Report report : reports) {
            ArrayList<String> row = new ArrayList<String>();
            row.add(String.valueOf(report.getId()));
            row.add(MessageUtils.getTag(FlareBot.getInstance().getUserByID(String.valueOf(report.getReporterId()))));
            row.add(MessageUtils.getTag(FlareBot.getInstance().getUserByID(String.valueOf(report.getReportedId()))));

            DateFormat formatedDate = new SimpleDateFormat("MM/dd/yyyy HH:mm"); //US format
            String date = formatedDate.format(report.getTime());

            row.add(date);

            table.add(row);
        }

        String reportsTable = MessageUtils.makeAsciiTable(header, table, footer);

        return reportsTable;
    }

    @Override
    public String getCommand() { return "reports"; }

    @Override
    public String getDescription() { return "Used to view reports and to report users"; }

    @Override
    public String getUsage() {
        return "{%}reports\n" +
                "{%}reports list [page]" +
                "{%}reports view <number>\n" +
                "{%}reports report <user> <reason>";
    }

    @Override
    public CommandType getType() { return CommandType.MODERATION; }
}
