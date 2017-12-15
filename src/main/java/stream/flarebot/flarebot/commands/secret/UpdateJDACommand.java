package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.scheduler.FlareBotTask;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class UpdateJDACommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1) {
            String version = args[0];

            try {
                File f = new File("pom.xml");
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);

                Node jdaNode = doc.getElementsByTagName("dependency").item(0);
                NodeList children = jdaNode.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node node = children.item(i);
                    if (node.getNodeName().equalsIgnoreCase("version"))
                        node.setTextContent(version);
                }

                DOMSource source = new DOMSource(doc);
                TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(f));
                channel.sendMessage("Updated to JDA version `" + version + "`\n" +
                        "I will now restart in 10 minutes and apply the update!").queue();
                new FlareBotTask("JDA-Update") {
                    @Override
                    public void run() {
                        FlareBot.getInstance().quit(true);
                    }
                }.delay(TimeUnit.MINUTES.toMillis(10));
                // _update-jda 3.3.1_306
            } catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
                FlareBot.LOGGER.error("Failed to parse the pom file!", e);
                channel.sendMessage("Parsing failed! " + e.getMessage()).queue();
            }
        }
    }

    @Override
    public String getCommand() {
        return "update-jda";
    }

    @Override
    public String getDescription() {
        return "Update JDA";
    }

    @Override
    public String getUsage() {
        return "{%}update-jda [version]";
    }

    @Override
    public CommandType getType() {
        return CommandType.SECRET;
    }
}
