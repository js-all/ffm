package dev.viandox.ffm.interpolation;

import dev.viandox.ffm.FFMUtils;

import java.time.Duration;
import java.util.function.Function;

public class InterpolableDouble extends Interpolable<Double> {
    public InterpolableDouble(double value, Duration transitionDuration, Function<Double, Double> timingFunction) {
        super(value, transitionDuration, timingFunction);
    }

    public static InterpolableDouble linear(double value, Duration transitionDuration) {
        return new InterpolableDouble(value, transitionDuration, t -> t);
    }
    public static InterpolableDouble linear(double value) {
        return new InterpolableDouble(value, t -> t);
    }
    public static InterpolableDouble easeInOut(double value, Duration transitionDuration) {
        return new InterpolableDouble(value, transitionDuration, FFMUtils::easeInOut);
    }
    public static InterpolableDouble easeInOut(double value) {
        return new InterpolableDouble(value, FFMUtils::easeInOut);
    }

    public InterpolableDouble(double value, Function<Double, Double> timingFunction) {
        super(value, timingFunction);
    }

    @Override
    protected Double lerp(double f) {
        // disgusting, truly
        return oldValue + f * (value - oldValue);
    }
}
