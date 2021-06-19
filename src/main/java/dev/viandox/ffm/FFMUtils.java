package dev.viandox.ffm;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.time.Duration;
import java.util.Map;

public class FFMUtils {
    public static Map<String, Long> suffixes = Maps.newHashMap();
    static {
        suffixes.put("k", 1_000L);
        suffixes.put("M", 1_000_000L);
        suffixes.put("G", 1_000_000_000L);
        suffixes.put("T", 1_000_000_000_000L);
        suffixes.put("P", 1_000_000_000_000_000L);
        suffixes.put("E", 1_000_000_000_000_000_000L);
    }
    public static long parseSuffixedNumber(String str) {
        double numberPart = Double.parseDouble(str.replaceAll("[^0-9-.]", ""));
        Long fac = suffixes.get(str.replaceAll("[0-9-.,]", ""));
        System.out.println(fac + "   =>  '" + str.replaceAll("[0-9-.,]", "") + "'");
        fac = fac == null ? 1 : fac;
        return (long) (numberPart * fac);
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
}
