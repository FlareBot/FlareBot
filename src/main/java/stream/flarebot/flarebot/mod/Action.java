package stream.flarebot.flarebot.mod;

import net.dv8tion.jda.core.entities.Message;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public enum Action {

    INVITE_LINKS(2, true, (message, config) -> MessageUtils.hasInvite(message)),
    SPAM(1, false, (message, config) -> FlareBot.getInstance().getAutoModTracker()
            .getMessages(message.getGuild().getId(), message.getAuthor().getId())
            >= FlareBot.getInstance().getManager().getGuild(message.getGuild().getId()).getAutoModConfig()
            .getMaxMessagesPerMinute()),
    LINKS(1, true, (message, config) -> MessageUtils.hasLink(message) && !MessageUtils.hasInvite(message)),
    PROFANITY(1, true, (message, config) -> FlareBot.getInstance().getManager().getProfanity().stream()
            .filter(word -> message.getContent().toLowerCase().contains(word))
            .count() > 0),
    DUPLICATED_CHARACTERS_OR_WORDS(1, true, (message, config) -> {
        Map<String, Integer> words = new HashMap<>();
        for (String word : message.getContent().toLowerCase().split(" ")) {
            if (MessageUtils.hasInvite(word) || MessageUtils.hasLink(word)) continue;
            words.put(word, words.containsKey(word) ? words.get(word) + 1 : 1);
            if (words.get(word) >= ((message.getContent().length() / 200) * 4 < 4 ? 4 : (message.getContent()
                    .length() / 200) * 4))
                return true;
            if (word.chars().mapToObj(i -> (char) i)
                    .collect(Collectors.groupingBy(Object::toString, Collectors.counting())).values().stream()
                    .filter(amt -> amt > 5).count() > 0)
                return true;
        }
        return false;
    }),
    TOO_MANY_CAPS(1, false, (message, config) -> message.getContent().replaceAll("[^a-zA-Z0-9]", "").length() > 4 && message
            .getContent().replaceAll("[^a-zA-Z0-9]", "").chars()
            .filter(Character::isUpperCase).count() > 0 && ((double) message.getContent().replaceAll("[^a-zA-Z0-9]", "")
            .chars().filter(Character::isUpperCase)
            .count()
            / message.getContent().replaceAll("[^a-zA-Z0-9]", "").length()) * 100 >= config.getOption("cap-percentage").intValue());
    //MASS_MENTION(5, false, (message, config) -> message.getMentionedUsers().size() >= config.getOption("mass_mention.max-user-mentions") || message.getMentionedRoles().size() >= 3);

    public static Action[] values = values();

    private int points;
    private boolean canBeWhitelisted;
    private BiPredicate<Message, AutoModConfig> check;

    Action(int points, boolean canBeWhitelisted, BiPredicate<Message, AutoModConfig> check) {
        this.points = points;
        this.canBeWhitelisted = canBeWhitelisted;
        this.check = check;
    }

    public int getDefaultPoints() {
        return this.points;
    }

    public boolean check(Message message, AutoModConfig config) {
        if (!FlareBotManager.getInstance().getGuild(message.getTextChannel().getGuild().getId()).getPermissions()
                .hasPermission(message.getGuild().getMember(message.getAuthor()), "flarebot.automod.bypass"))
            return this.check.test(message, config);
        else
            return false;
    }

    public String getName() {
        return name().charAt(0) + name().substring(1).toLowerCase().replaceAll("_", " ");
    }

    public String getNameWithUnderscore() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public boolean canBeWhitelisted() {
        return this.canBeWhitelisted;
    }

    public static Action getAction(String action) {
        for (Action action1 : values) {
            if (action1.getName().equalsIgnoreCase(action) || action1.getNameWithUnderscore().equalsIgnoreCase(action))
                return action1;
        }
        return null;
    }
}
