package com.bwfcwalshy.flarebot.permissions;

import java.util.function.Predicate;

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
        String textNode = getNode().replace(".", "\\.").replace("*", ".*");
        return node.getNode().matches(textNode);
    }
}
