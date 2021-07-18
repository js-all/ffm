package dev.viandox.ffm;

import dev.viandox.ffm.config.Config;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

public abstract class Interpolable<T> {
    private T oldValue;
    private T value;
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

    public static <A> Interpolable<A> linear(A value, Duration transitionDuration) {
        return new Interpolable<A>(value, transitionDuration, t -> t);
    }
    public static <A> Interpolable<A> linear(A value) {
        return new Interpolable<A>(value, t -> t);
    }
    public static <A> Interpolable<A> easeInOut(A value, Duration transitionDuration) {
        return new Interpolable<A>(value, transitionDuration, FFMUtils::easeInOut);
    }
    public static <A> Interpolable<A> easeInOut(A value) {
        return new Interpolable<A>(value, FFMUtils::easeInOut);
    }

    public void set(T value) {
        this.oldValue = this.value;
        this.value = value;
        this.lastChange = Instant.now();
    }

    abstract protected T lerp(double f);

    public T get() {
        double f = (((double) Instant.now().toEpochMilli()) - ((double) this.lastChange.toEpochMilli())) / this.transitionDuration.toMillis();
        f = Math.min(f, 1d);
        return this.lerp(timingFunction.apply(f));
    }
}
