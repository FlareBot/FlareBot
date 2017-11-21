package stream.flarebot.flarebot.util.currency;

import io.github.binaryoverload.JSONConfig;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import okhttp3.Response;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.util.WebUtils;

import java.io.IOException;
import java.util.Random;

public class CurrencyConversionUtil {

    private static Random random = new Random();

    public static boolean normalEndpointAvailable() {
        return WebUtils.pingHost(CurrencyApiRoutes.NormalApi.LATEST_ALL.getCompiledUrl(), 300);
    }

    public static boolean cryptoEndpintAvailable() {
        return WebUtils.pingHost(CurrencyApiRoutes.CrytoApi.LIST_CURRENCIES.getCompiledUrl(), 300);
    }

    public static boolean isValidCurrency(String currency) throws IOException {
        return isValidCurrency(currency, false);
    }

    public static boolean isValidCurrency(String currency, boolean nonCrypto) throws IOException {
        if (!normalEndpointAvailable()) return isValidCurrency(currency) && !nonCrypto;
        Response res = WebUtils.get(CurrencyApiRoutes.NormalApi.LATEST_ALL.getCompiledUrl());
        if (!res.isSuccessful() || res.body() == null) {
            return isValidCrytoCurrency(currency) && !nonCrypto;
        }
        JSONConfig config = new JSONConfig(res.body().byteStream());
        if (config.getSubConfig("rates").isPresent()) {
            JSONConfig rates = config.getSubConfig("rates").get();
            return rates.getKeys(false).contains(currency.toUpperCase()) || (isValidCrytoCurrency(currency) && !nonCrypto);
        }
        return false;
    }

    public static Boolean isValidCrytoCurrency(String currency) throws IOException {
        if (!cryptoEndpintAvailable()) return false;
        Response res = WebUtils.get(CurrencyApiRoutes.CrytoApi.BASIC_TICKER.getCompiledUrl("usd", currency));
        if (!res.isSuccessful() || res.body() == null) throw new IOException();

        JSONConfig config = new JSONConfig(res.body().byteStream());
        if (config.getBoolean("success").isPresent()) {
            return config.getBoolean("success").get();
        }
        return false;
    }

    public static CurrencyComparison getCurrencyComparison(TextChannel channel, User sender, Command cmd,
                                                           String from, String to) {
        try {
            if (!isValidCurrency(from))
                throw new IllegalArgumentException("`" + from.toUpperCase() + "` is not a valid currency!");

            if (!isValidCurrency(to))
                throw new IllegalArgumentException("`" + to.toUpperCase() + "` is not a valid currency!");

            if (from.equalsIgnoreCase(to)) {
                if ((random.nextInt(100) + 1) == 100) {
                    channel.sendMessage("I had hoped you didn't need me for that...").queue();
                    FlareBot.getInstance().logEG("Convert a currency to itself...", cmd, channel.getGuild(), sender);
                }
                return new CurrencyComparison(from, to, (double) 1);
            }

            CurrencyComparison normalCurrency = CurrencyConversionUtil.getNormalCurrency(from, to);
            if (normalCurrency != null) return normalCurrency;
            else return getCryptoCurrency(from, to);

        } catch (IOException e) {
            return null;
        }
    }

    private static CurrencyComparison getCryptoCurrency(String from, String to) throws IOException {
        Response res = WebUtils.get(CurrencyApiRoutes.CrytoApi.BASIC_TICKER.getCompiledUrl(from, to));
        if (!res.isSuccessful() || res.body() == null) return null;

        JSONConfig config = new JSONConfig(res.body().byteStream());
        res.close();
        if (config.getBoolean("success").isPresent() && config.getBoolean("success").get()) {
            String price = config.getString("ticker.price").get();
            Double priceDouble;
            try {
                priceDouble = Double.parseDouble(price);
            } catch (NumberFormatException e) {
                return null;
            }
            return new CurrencyComparison(from, to, priceDouble);
        }
        return null;
    }

    private static CurrencyComparison getNormalCurrency(String from, String to) throws IOException {
        Response response =
                WebUtils.get(CurrencyApiRoutes.NormalApi.LATEST_WITH_SYMBOLS_AND_BASE.getCompiledUrl(to, from));
        if (!response.isSuccessful() || response.body() == null) return null;
        JSONConfig config = new JSONConfig(response.body().byteStream());
        response.close();
        if (config.getString("base").isPresent() && config.getDouble("rates." + to).isPresent()) {
            Double conversion = config.getDouble("rates." + to).getAsDouble();
            return new CurrencyComparison(from, to, conversion);
        }
        return null;
    }
}
