package dev.viandox.ffm.interpolation;

import dev.viandox.ffm.FFMUtils;

import java.time.Duration;
import java.util.function.Function;

public class InterpolableFloat extends Interpolable<Float> {
    public InterpolableFloat(float value, Duration transitionDuration, Function<Double, Double> timingFunction) {
        super(value, transitionDuration, timingFunction);
    }
    public InterpolableFloat(float value, Function<Double, Double> timingFunction) {
        super(value, timingFunction);
    }

    public static InterpolableFloat linear(float value, Duration transitionDuration) {
        return new InterpolableFloat(value, transitionDuration, t -> t);
    }
    public static InterpolableFloat linear(float value) {
        return new InterpolableFloat(value, t -> t);
    }
    public static InterpolableFloat easeInOut(float value, Duration transitionDuration) {
        return new InterpolableFloat(value, transitionDuration, FFMUtils::easeInOut);
    }
    public static InterpolableFloat easeInOut(float value) {
        return new InterpolableFloat(value, FFMUtils::easeInOut);
    }

    @Override
    protected Float lerp(double f) {
        // disgusting, truly
        return (float)(oldValue + f * (value - oldValue));
    }
}
