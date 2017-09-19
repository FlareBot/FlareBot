package stream.flarebot.flarebot.util.currency;

public class CurrencyComparison {

    private final String base;
    private final String to;
    private final Double rate;

    public CurrencyComparison(String base, String to, Double rate) {
        this.base = base.toUpperCase();
        this.to = to.toUpperCase();
        this.rate = rate;
    }

    public Double getRate() {
        return rate;
    }

    public String getTo() {
        return to;
    }

    public String getBase() {
        return base;
    }
}
