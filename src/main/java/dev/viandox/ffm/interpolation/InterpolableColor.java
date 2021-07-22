package dev.viandox.ffm.interpolation;

import dev.viandox.ffm.ColorConverter;
import dev.viandox.ffm.FFMUtils;

import java.time.Duration;
import java.util.function.Function;

public class InterpolableColor extends Interpolable<int[]> {
    public InterpolableColor(int[] value, Duration transitionDuration, Function<Double, Double> timingFunction) {
        super(value, transitionDuration, timingFunction);
    }

    public InterpolableColor(int[] value, Function<Double, Double> timingFunction) {
        super(value, timingFunction);
    }

    public InterpolableColor(int value, Duration transitionDuration, Function<Double, Double> timingFunction) {
        super(ColorConverter.INTtoRGBA(value), transitionDuration, timingFunction);
    }

    public InterpolableColor(int value, Function<Double, Double> timingFunction) {
        super(ColorConverter.INTtoRGBA(value), timingFunction);
    }

    public static InterpolableColor linear(int value, Duration transitionDuration) {
        return new InterpolableColor(value, transitionDuration, t -> t);
    }
    public static InterpolableColor linear(int value) {
        return new InterpolableColor(value, t -> t);
    }
    public static InterpolableColor easeInOut(int value, Duration transitionDuration) {
        return new InterpolableColor(value, transitionDuration, FFMUtils::easeInOut);
    }
    public static InterpolableColor easeInOut(int value) {
        return new InterpolableColor(value, FFMUtils::easeInOut);
    }

    public static InterpolableColor linear(int[] value, Duration transitionDuration) {
        return new InterpolableColor(value, transitionDuration, t -> t);
    }
    public static InterpolableColor linear(int[] value) {
        return new InterpolableColor(value, t -> t);
    }
    public static InterpolableColor easeInOut(int[] value, Duration transitionDuration) {
        return new InterpolableColor(value, transitionDuration, FFMUtils::easeInOut);
    }
    public static InterpolableColor easeInOut(int[] value) {
        return new InterpolableColor(value, FFMUtils::easeInOut);
    }

    public void set(int value) {
        super.set(ColorConverter.INTtoRGBA(value));
    }

    public int getInt() {
        return ColorConverter.RGBAtoINT(super.get());
    }

    @Override
    protected int[] lerp(double f) {
        return ColorConverter.lerpRGBA(oldValue, value, (float) f);
    }
}
