package dev.viandox.ffm;

import net.minecraft.client.util.math.Vector3f;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class HudConfig {
    public static Duration playerAbilityDisplayDuration = Duration.of(3, ChronoUnit.SECONDS);
    public static Duration playerAbilityTransitionDuration = Duration.of(100, ChronoUnit.MILLIS);
    public static double interpolationPoint1 = 0;
    public static double interpolationPoint2 = 1;
    public static float localMapScale = 0.9f;
    public static float globalMapScale = 1.25f;
    public static Vector3f globalMapTranslate = new Vector3f(10, 10, 0);
    public static Duration mapChangeInterval = Duration.of(250, ChronoUnit.MILLIS);
    public static float mapArrowButtHeight = 3;
    public static float mapArrowHeight = 11;
    public static float mapArrowWidth = 8;
    public static float mapArrowOutlineWidth = 1.5f;
    public static int mapArrowOutlineColor = 0x44000000;
}
