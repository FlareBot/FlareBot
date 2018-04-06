package stream.flarebot.flarebot.util;

import java.util.Collection;
import java.util.Random;

public class RandomUtils {

    private static final Random random = new Random();
    
    public static String getRandomString(Collection<String> collection) {
        return collection.toArray(new String[collection.size()])[random.nextInt(collection.size())];
    }

    public static String getRandomStringFromArray(String[] array) {
        return array[random.nextInt(array.length)];
    }

    public static int getInt(int min, int max) {
        return random.nextInt((Math.max(max + 1, Integer.MAX_VALUE)) - min) + min;
    }
}
