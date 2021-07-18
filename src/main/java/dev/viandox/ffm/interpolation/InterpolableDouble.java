package dev.viandox.ffm.interpolation;

import dev.viandox.ffm.FFMUtils;

import java.time.Duration;
import java.util.function.Function;

public class InterpolableNumber<T extends Number> extends Interpolable<T> {
    public InterpolableNumber(T value, Duration transitionDuration, Function<Double, Double> timingFunction) {
        super(value, transitionDuration, timingFunction);
    }

    public static <A extends Number> InterpolableNumber<A> linear(A value, Duration transitionDuration) {
        return new InterpolableNumber<A>(value, transitionDuration, t -> t);
    }
    public static <A extends Number> InterpolableNumber<A> linear(A value) {
        return new InterpolableNumber<A>(value, t -> t);
    }
    public static <A extends Number> InterpolableNumber<A> easeInOut(A value, Duration transitionDuration) {
        return new InterpolableNumber<A>(value, transitionDuration, FFMUtils::easeInOut);
    }
    public static <A extends Number> InterpolableNumber<A> easeInOut(A value) {
        return new InterpolableNumber<A>(value, FFMUtils::easeInOut);
    }

    public InterpolableNumber(T value, Function<Double, Double> timingFunction) {
        super(value, timingFunction);
    }

    @Override
    protected T lerp(double f) {
        // disgusting, truly
        return (T) (Double.valueOf(oldValue.doubleValue() + f * (value.doubleValue() - oldValue.doubleValue())));
    }
}
