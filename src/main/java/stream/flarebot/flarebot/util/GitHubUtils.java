package stream.flarebot.flarebot.util;

import net.dv8tion.jda.core.EmbedBuilder;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONObject;
import stream.flarebot.flarebot.FlareBot;

import java.io.IOException;
import java.util.Arrays;

public class GitHubUtils {

    public static EmbedBuilder getEmbedForPR(String prNum) {
        JSONObject obj;
        try {
            Response res = WebUtils.get("https://api.github.com/repos/FlareBot/FlareBot/pulls/" + prNum);
            ResponseBody body = res.body();

            if (body != null) {
                obj = new JSONObject(body.string());
                body.close();
            } else {
                res.close();
                FlareBot.LOGGER.error("GitHub returned an empty response - Code " + res.code());
                return null;
            }
            res.close();
        } catch (IOException e) {
            FlareBot.LOGGER.error("Error getting the PR info! " + e.getMessage(), e);
            return null;
        }

        String body = obj.getString("body");
        String[] array = body.split("\r\n\r\n");

        String title = array[0].split("\r\n")[0].replace("### ", "");
        String description = array[0].split("\r\n")[1];

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title, null);
        embed.setDescription(description);

        array = Arrays.copyOfRange(array, 1, array.length);

        for (String anArray : array) {
            String value = anArray.replaceAll("\n\\* ", "\n\u2022 ");
            String header = value.replace("## ", "").substring(0, value.indexOf("\n") - 4).replace("\n", "");

            value = value.replace("## " + header, "");

            if (value.length() > 1024) {
                embed.addField(header, value.substring(0, value.substring(0, 1024).lastIndexOf("\n")), false);
                value = value.substring(value.substring(0, 1024).lastIndexOf("\n") + 1);
                header += " - Continued";
            }

            embed.addField(header, value, false);
        }

        return embed;
    }
}
