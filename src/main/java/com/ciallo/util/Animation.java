package com.ciallo.util;

/**
 * One shot time based animation, ported from the original mod's Animation utility.
 */
public class Animation {
    private long startTime = System.currentTimeMillis();
    private double target = 0.0;

    public Animation() {
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
        this.target = 0.0;
    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * @param target   value to animate towards (usually 1.0)
     * @param duration duration in milliseconds
     * @param easing   easing curve
     * @return eased progress in the 0..1 range; returns the target once the animation is done
     */
    public double get(double target, long duration, Easing easing) {
        if (this.target != target) {
            this.target = target;
            this.startTime = System.currentTimeMillis();
        }
        long elapsed = System.currentTimeMillis() - startTime;
        if (duration <= 0L) {
            return target;
        }
        double progress = Math.min(1.0, elapsed / (double) duration);
        return target * easing.ease(progress);
    }

    public double get(long duration, Easing easing) {
        return get(1.0, duration, easing);
    }
}
