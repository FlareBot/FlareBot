package stream.flarebot.flarebot.web;

import spark.Request;

import java.util.function.Predicate;

public class Require {
    private String name;
    protected Predicate<String> test;

    public Require(String name, Predicate<String> test) {
        this.name = name;
        this.test = test;
    }

    public Require(String name) {
        this(name, f -> true);
    }

    public String getName() {
        return name;
    }

    public boolean verify(Request request) {
        return request.queryParams(name) != null && test.test(request.queryParams(name));
    }
}
