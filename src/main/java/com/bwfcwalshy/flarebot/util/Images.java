package com.bwfcwalshy.flarebot.util;

import com.bwfcwalshy.flarebot.FlareBot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class Images {
    public static Color averageColor(BufferedImage bi) {
        int red = 0, green = 0, blue = 0;
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                Color pixel = new Color(bi.getRGB(x, y));
                red += pixel.getRed();
                green += pixel.getGreen();
                blue += pixel.getBlue();
            }
        }
        int pixels = bi.getHeight() * bi.getWidth();
        return new Color(red / pixels, green / pixels, blue / pixels);
    }

    public static BufferedImage imageFor(String url) {
        try {
            URL urll = new URL(url);
            URLConnection connection = urll.openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();
            return ImageIO.read(connection.getInputStream());
        } catch (IOException e) {
            FlareBot.LOGGER.error(String.format("Could not get BufferedImage for '%s'", url), e);
            return null;
        }
    }
}
