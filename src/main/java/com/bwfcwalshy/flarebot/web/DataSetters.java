package com.bwfcwalshy.flarebot.web;

import com.bwfcwalshy.flarebot.FlareBot;
import spark.Request;
import spark.Response;

import java.util.function.BiFunction;

public enum DataSetters {
    ;

    private BiFunction<Request, Response, Object> consumer;

    DataSetters(BiFunction<Request, Response, Object> o) {
        consumer = o;
    }

    public String process(Request request, Response response) {
        return FlareBot.GSON.toJson(consumer.apply(request, response));
    }
}
