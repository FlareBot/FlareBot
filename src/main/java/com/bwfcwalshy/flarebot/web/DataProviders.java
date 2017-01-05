package com.bwfcwalshy.flarebot.web;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.web.objects.Songs;
import spark.Request;
import spark.Response;

import java.util.function.BiFunction;

public enum DataProviders {
    SONGS((req, res) -> Songs.get());

    private BiFunction<Request, Response, Object> consumer;

    DataProviders(BiFunction<Request, Response, Object> o) {
        consumer = o;
    }

    public Object provide(Request request, Response response) {
        return FlareBot.GSON.toJson(consumer.apply(request, response));
    }
}
