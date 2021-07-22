package dev.viandox.ffm.interpolation;

import dev.viandox.ffm.FFMUtils;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.MathHelper;

import java.time.Duration;
import java.util.function.Function;

public class InterpolableVector3f extends Interpolable<Vector3f> {
    public InterpolableVector3f(Vector3f value, Duration transitionDuration, Function<Double, Double> timingFunction) {
        super(value, transitionDuration, timingFunction);
    }

    public InterpolableVector3f(Vector3f value, Function<Double, Double> timingFunction) {
        super(value, timingFunction);
    }

    public static InterpolableVector3f linear(Vector3f value, Duration transitionDuration) {
        return new InterpolableVector3f(value, transitionDuration, t -> t);
    }
    public static InterpolableVector3f linear(Vector3f value) {
        return new InterpolableVector3f(value, t -> t);
    }
    public static InterpolableVector3f easeInOut(Vector3f value, Duration transitionDuration) {
        return new InterpolableVector3f(value, transitionDuration, FFMUtils::easeInOut);
    }
    public static InterpolableVector3f easeInOut(Vector3f value) {
        return new InterpolableVector3f(value, FFMUtils::easeInOut);
    }

    @Override
    protected Vector3f lerp(double f) {
        Vector3f res = oldValue.copy();
        res.lerp(value, (float) f);
        return res;
    }
}
