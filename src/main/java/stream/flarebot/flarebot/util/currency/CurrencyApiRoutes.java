package stream.flarebot.flarebot.util.currency;

import stream.flarebot.flarebot.util.objects.ApiRoute;

public class CurrencyApiRoutes {

    public static class NormalApi {

        private static final String BASE_URL = "https://api.fixer.io";

        public static final ApiRoute LATEST_ALL = new ApiRoute(BASE_URL + "/latest");
        public static final ApiRoute LATEST_WITH_BASE = new ApiRoute(BASE_URL + "/latest?base={base}");
        public static final ApiRoute LATEST_WITH_SYMBOLS = new ApiRoute(BASE_URL + "/latest?symbols={symbols}");
        public static final ApiRoute LATEST_WITH_SYMBOLS_AND_BASE =
                new ApiRoute(BASE_URL + "/latest?symbols={symbols}&base={base}");

        public static final ApiRoute DATE_ALL = new ApiRoute(BASE_URL + "/{date}");
        public static final ApiRoute DATE_WITH_BASE = new ApiRoute(BASE_URL + "/{date}?base={base}");
        public static final ApiRoute DATE_WITH_SYMBOLS = new ApiRoute(BASE_URL + "/{date}?symbols={symbols}");
        public static final ApiRoute DATE_WITH_SYMBOLS_AND_BASE =
                new ApiRoute(BASE_URL + "/{date}?symbols={symbols}&base={base}");
    }

    public static class CrytoApi {

        private static final String BASE_URL = "https://api.cryptonator.com/api";

        public static final ApiRoute BASIC_TICKER = new ApiRoute(BASE_URL + "/ticker/{from}-{to}");
        public static final ApiRoute FULL_TICKER = new ApiRoute(BASE_URL + "/full/{from}-{to}");

        public static final ApiRoute LIST_CURRENCIES = new ApiRoute(BASE_URL + "/currencies");
    }

}
