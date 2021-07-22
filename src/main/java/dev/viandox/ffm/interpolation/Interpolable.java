package dev.viandox.ffm.interpolation;

import dev.viandox.ffm.FFMUtils;
import dev.viandox.ffm.config.Config;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

public abstract class Interpolable<T> {
    protected T oldValue;
    protected T value;
    private final Function<Double, Double> timingFunction;
    private final Duration transitionDuration;
    private Instant lastChange = Instant.ofEpochMilli(0);
    public Interpolable(T value, Duration transitionDuration, Function<Double, Double> timingFunction) {
        this.value = value;
        this.oldValue = value;
        this.timingFunction = timingFunction;
        this.transitionDuration = transitionDuration;
    }

    public Interpolable(T value, Function<Double, Double> timingFunction) {
        this.value = value;
        this.oldValue = value;
        this.timingFunction = timingFunction;
        this.transitionDuration = Config.transitionDuration;
    }

    public void set(T value) {
        this.oldValue = this.get();
        this.value = value;
        this.lastChange = Instant.now();
    }

    public void setWithoutInterpolation(T value) {
        this.oldValue = value;
        this.set(value);
    }

    abstract protected T lerp(double f);

    public double getInterpolationProgress() {
        double f = (((double) Instant.now().toEpochMilli()) - ((double) this.lastChange.toEpochMilli())) / this.transitionDuration.toMillis();
        return Math.min(f, 1d);
    }

    public T get() {
        return this.lerp(timingFunction.apply(this.getInterpolationProgress()));
    }
}
