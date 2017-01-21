package com.bwfcwalshy.flarebot.permissions;

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
        String textNode = Arrays.stream(getNode().split("(?:^\\*(\\.))|(?:(?<=\\.)\\*(?=\\.))"))
                .map(Pattern::quote)
                .collect(Collectors.joining(".+"));
        return node.getNode().matches(textNode);
    }
}
