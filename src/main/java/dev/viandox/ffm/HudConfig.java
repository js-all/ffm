package dev.viandox.ffm;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class HudConfig {
    public static Duration playerAbilityDisplayDuration = Duration.of(3, ChronoUnit.SECONDS);
    public static Duration playerAbilityTransitionDuration = Duration.of(100, ChronoUnit.MILLIS);
    public static double interpolationPoint1 = 0;
    public static double interpolationPoint2 = 1;
}
