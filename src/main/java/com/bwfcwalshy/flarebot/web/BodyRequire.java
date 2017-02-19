package com.bwfcwalshy.flarebot.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import spark.Request;

import java.util.function.Predicate;

public class BodyRequire extends Require {
    private Predicate<JsonElement> eTest = r -> true;
    JsonParser parser = new JsonParser();

    public BodyRequire(String name, Predicate<String> test) {
        super(name, test);
    }

    public BodyRequire(Predicate<JsonElement> test, String name) {
        this(name);
        this.eTest = test;
    }

    public BodyRequire(String name) {
        super(name);
    }

    @Override
    public boolean verify(Request request) {
        try {
            JsonElement element = parser.parse(request.body());
            return element.isJsonObject() && element.getAsJsonObject().has(getName())
                    && test.test(String.valueOf(element.getAsJsonObject().get(getName())))
                    && eTest.test(element.getAsJsonObject().get(getName()));
        } catch (Exception e) {
            return false;
        }
    }
}
