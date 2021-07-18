package dev.viandox.ffm;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.time.Duration;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class FFMUtils {
    public static Map<String, Long> suffixesSL = Maps.newHashMap();
    private static final NavigableMap<Long, String> suffixesLS = new TreeMap<>();
    static {
        suffixesSL.put("k", 1_000L);
        suffixesSL.put("M", 1_000_000L);
        suffixesSL.put("B", 1_000_000_000L);
        suffixesSL.put("T", 1_000_000_000_000L);
        suffixesLS.put(1_000L, "k");
        suffixesLS.put(1_000_000L, "M");
        suffixesLS.put(1_000_000_000L, "B");
        suffixesLS.put(1_000_000_000_000L, "T");
    }

    public static long parseSuffixedNumber(String str) {
        double numberPart = 1;
        try {
            numberPart = Double.parseDouble(str.replaceAll("[^0-9-.]", ""));
        } catch (Error e) {}
        Long fac = suffixesSL.get(str.replaceAll("[0-9-.,]", ""));
        fac = fac == null ? 1 : fac;
        return (long) (numberPart * fac);
    }

    public static String formatSuffixedNumber(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return formatSuffixedNumber(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + formatSuffixedNumber(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixesLS.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        long nanos = duration.toNanos() % 1_000_000_000L;
        long ms = Math.abs(nanos / 1_000_000);
        String positive = String.format(
                "%d:%02d:%02d.%03d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60,
                ms);
        return seconds < 0 ? "-" + positive : positive;
    }

    public static boolean isItemStackBackgroundGlassPane(ItemStack itemStack) {
        return itemStack.getItem() == Registry.ITEM.get(new Identifier("minecraft:black_stained_glass_pane")) && itemStack.getName().getString().equals(" ");
    }

    public static double easeInOut(double t) {
        double sqt = t * t;
        return sqt / (2.0d * (sqt - t) + 1.0d);
    }

    public static class BoundingBox {
        public float x0;
        public float y0;
        public float x1;
        public float y1;

        public BoundingBox(float x0, float y0, float x1, float y1) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        }

        public float getWidth() {
            return Math.abs(x1 - x0);
        }

        public float getHeight() {
            return Math.abs(y1 - y0);
        }

        public boolean isPointWithin(float x, float y) {
            return x <= x1 && x >= x0 && y <= y1 && y >= y0;
        }

        public boolean isColliding(BoundingBox other) {
            return other.x0 < this.x1 && other.x1 > this.x0 && other.y0 < this.y1 && other.y1 > this.y0;
        }

        public float[] forcePointInside(float x, float y) {
            return new float[] {
                    Math.min(Math.max(x, this.x0), this.x1),
                    Math.min(Math.max(y, this.y0), this.y1)
            };
        }
    }
}
