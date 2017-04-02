package com.bwfcwalshy.flarebot.mod;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import net.dv8tion.jda.core.entities.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum Action {

    INVITE_LINKS(2, MessageUtils::hasInvite),
    SPAM(1, message -> FlareBot.getInstance().getAutoModTracker().getMessages(message.getGuild().getId(), message.getAuthor().getId())
            >= FlareBot.getInstance().getManager().getAutoModConfig(message.getGuild().getId()).getMaxMessagesPerMinute()),
    LINKS(1, MessageUtils::hasLink),
    PROFANITY(1, message -> FlareBot.getInstance().getManager().getProfanity().stream().filter(word -> message.getContent().toLowerCase().contains(word)).count() > 0),
    DUPLICATED_CHARACTERS_OR_WORDS(1, message -> {
        Map<String, Integer> words = new HashMap<>();
        for(String word : message.getContent().toLowerCase().split(" ")){
            words.put(word, words.containsKey(word) ? words.get(word) + 1 : 1);
            if(words.get(word) >= ((message.getContent().length() / 200) * 4 < 4 ? 4 : (message.getContent().length() / 200) * 4))
                return true;
            if(word.chars().mapToObj(i -> (char) i).collect(Collectors.groupingBy(Object::toString, Collectors.counting())).values().stream().filter(amt -> amt > 3).count() > 0)
                return true;
        }
        return false;
    }),
    TOO_MANY_CAPS(1, message -> message.getContent().replaceAll("[^a-zA-Z0-9]", "").length() > 4 && message.getContent().replaceAll("[^a-zA-Z0-9]", "").chars()
            .filter(Character::isUpperCase).count() > 0 && ((double) message.getContent().replaceAll("[^a-zA-Z0-9]", "").chars().filter(Character::isUpperCase).count()
            / message.getContent().replaceAll("[^a-zA-Z0-9]", "").length()) * 100 > 30);

    public static Action[] values = values();

    private int points;
    private Predicate<Message> check;

    Action(int points, Predicate<Message> check){
        this.points = points;
        this.check = check;
    }

    public int getDefaultPoints(){
        return this.points;
    }

    public boolean check(Message message) {
        if(!FlareBot.getInstance().getPermissions(message.getTextChannel()).hasPermission(message.getGuild().getMember(message.getAuthor()), "flarebot.automod.bypass"))
            return this.check.test(message);
        else
            return false;
    }

    public String getName() {
        return name().charAt(0) + name().substring(1).toLowerCase().replaceAll("_", " ");
    }
}
