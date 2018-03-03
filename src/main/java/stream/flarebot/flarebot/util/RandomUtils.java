package stream.flarebot.flarebot.util;

import java.util.Random;

public class RandomUtils {

    private final Random random = new Random();
    
    public static String getRandomString(Collection<String> collection) {
        return collection.toArray(new String[collection.size()]{})[random.nextInt(collection.size())];
    }
}
