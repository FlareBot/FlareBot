package stream.flarebot.flarebot.util.objects;

import org.apache.commons.lang3.StringUtils;

public class ApiRoute {

    private String url;
    private int params;

    public String getUrl() {
        return url;
    }

    public int getParams() {
        return params;
    }

    public ApiRoute(String url) {
        int open = StringUtils.countMatches(url, "{");
        if (open != StringUtils.countMatches(url, "}")) {
            throw new IllegalArgumentException("Number of { does not match number of }");
        }

        this.url = url.replaceAll("\\{.*?}", "%s");
        this.params = open;
    }

    public String getCompiledUrl(String... params) {
        if (this.params != params.length) {
            throw new IllegalArgumentException(String.format("Number of params given (%d) does not match required amount (%d) !", params.length, this.params));
        }
        return String.format(this.url, params);
    }
}
