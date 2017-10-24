package stream.flarebot.flarebot.util;

import org.apache.commons.lang3.StringUtils;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.general.CommandUsageCommand;
import stream.flarebot.flarebot.commands.general.PollCommand;
import stream.flarebot.flarebot.commands.general.TagsCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsageParser {

    public static void test(String... ss) {
        for (String s : matchUsage(new PollCommand(), ss)) {
            FlareBot.getInstance().getImportantLogChannel().sendMessage(s).queue();
        }
    }

    public static List<String> matchUsage(Command c, String[] args) {
        List<String> strings = new ArrayList<>();
        String[] usages = c.getUsage().split("\n");
        if (args.length == 0) {
            Collections.addAll(strings, usages);
            return strings;
        }
        for (String usage : usages) {
            if (usage.isEmpty()) {
                strings.add(usage);
                continue;
            }
            Pattern p = Pattern.compile("`.+`");
            String symbols = usage.replace("{%}" + c.getCommand(), "").trim(); // Strip command out
            Matcher m = p.matcher(symbols);
            if (m.find()) {
                symbols = m.group().replace("`", "").trim();
            } else {
                continue;
            }
            Map<Integer, Pair<Symbol, String>> map = findSymbols(symbols);
            boolean applicable = false;
            if (args.length > map.size()) { // If there are more args than symbols it wouldn't be applicable
                continue;
            }
            for (Map.Entry<Integer, Pair<Symbol, String>> entry : map.entrySet()) {
                if (entry.getValue().getKey() == Symbol.SINGLE_SUB_COMMAND) {
                    if (args[entry.getKey()].equalsIgnoreCase(entry.getValue().getValue())) {
                        applicable = true; // Sub command matches arg
                    } else {
                        applicable = false;
                        break; // We don't want to check any other args if this fails
                    }
                } else if (entry.getValue().getKey() == Symbol.MULTIPLE_SUB_COMMAND) {
                    boolean valid = false;
                    for (String cmd : entry.getValue().getValue().split("\\|")) {
                        if (args[entry.getKey()].equalsIgnoreCase(cmd)) {
                            applicable = true;
                            valid = true;
                            break;
                        }
                    }
                    if (!valid) {
                        applicable = false;
                        break; // CHeck nothing else if this fails
                    }
                } else {
                    applicable = true;
                }
                if (args.length - 1 >= entry.getKey()) {
                    break;
                }
            }

            if (applicable) strings.add(usage);
        }
        return strings;
    }

    private static Map<Integer, Pair<Symbol, String>> findSymbols(String string) {
        Map<Integer, Pair<Symbol, String>> map = new HashMap<>();
        int i = 0;
        for (String s : string.split(" ")) {
            for (Symbol sy : Symbol.values()) {
                if (sy.matches(s)) map.put(i, new Pair<>(sy, s));
            }
            i++;
        }
        return map;
    }


    public enum Symbol {
        SINGLE_SUB_COMMAND(Pattern.compile("^[A-Za-z]+$")),
        MULTIPLE_SUB_COMMAND(Pattern.compile("^[A-z]+(\\|+[A-z]+)+$")),
        REQUIRED_ARG(Pattern.compile("^<.+>$")),
        OPTIONAL_ARG(Pattern.compile("^\\[.+\\]$"));

        private Pattern regex;

        Symbol(Pattern regex) {
            this.regex = regex;
        }

        public Pattern getRegex() {
            return regex;
        }

        public boolean matches(String arg) {
            return getRegex().matcher(arg).matches();
        }

    }

}
