package stream.flarebot.flarebot.permissions;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PermissionNode implements Predicate<PermissionNode> {

    private final String node;

    public PermissionNode(String node) {
        this.node = node;
    }

    public String getNode() {
        return node;
    }

    @Override
    public boolean test(PermissionNode node) {
        if (getNode().equals("*"))
            return true;
        // It splits by a `*` that's on a start of a string or has . around them
        String textNode = Arrays.stream(getNode().split("(?:^\\*(\\.))|(?:(?<=\\.)\\*(?=\\.))|(?:(?<=\\.)\\*$)"))
                // Then it escapes all of that so its not regexps
                .map(Pattern::quote)
                // And then it joins them with a match all regexp
                .collect(Collectors.joining(".+")) + (getNode().endsWith("*") ? ".+" : "");
        // And then it lets Java REGEXP compare them. Ty @I-Al-Istannen for making me do this comment
        return node.getNode().matches(textNode);
    }
}
