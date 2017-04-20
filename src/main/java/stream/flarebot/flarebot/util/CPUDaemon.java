package stream.flarebot.flarebot.util;

import stream.flarebot.flarebot.FlareBot;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

public final class CPUDaemon {
    private CPUDaemon() {
    }

    private static final AtomicReference<Double> PERCENTAGE = new AtomicReference<>(-1d);
    private static final String PROCPATH = "/proc/stat";
    private static final File PROCFILE = new File(PROCPATH);
    private static final Thread RUNNER = new Thread(new ThreadRunnable(), "CPU Daemon Runner Thread");

    static {
        RUNNER.start();
    }

    public static double get() {
        return PERCENTAGE.get();
    }

    private static class ThreadRunnable implements Runnable {

        @Override
        public void run() {
            if (PROCFILE.exists() && PROCFILE.getAbsolutePath().equals(PROCPATH))
                try {
                    while (!Thread.interrupted()) {
                        String cpu = Files.readAllLines(PROCFILE.toPath()).get(0);
                        String[] astrs = cpu.split("\\s+");
                        double[] a = new double[astrs.length];
                        for(int i = 1; i < astrs.length; i++)
                            a[i] = Double.parseDouble(astrs[i]);

                        Thread.sleep(500);
                        String cpu2 = Files.readAllLines(PROCFILE.toPath()).get(0);
                        String[] bstrs = cpu2.split("\\s+");
                        double[] b = new double[bstrs.length];
                        for(int i = 1; i < bstrs.length; i++)
                            b[i] = Double.parseDouble(bstrs[i]);

                        double loadavg = ((b[1] + b[2] + b[3]) - (a[1] + a[2] + a[3])) /
                                ((b[1] + b[2] + b[3] + b[4]) - (a[1] + a[2] + a[3] + a[4]));
                        PERCENTAGE.set(loadavg);
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    FlareBot.LOGGER.error("Error on CPU daemon", e);
                }
        }
    }
}
