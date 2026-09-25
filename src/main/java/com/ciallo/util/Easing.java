package com.ciallo.util;

/**
 * Easing functions used by animated render modules (ported from the original mod).
 */
public enum Easing {
    Linear {
        @Override
        public double apply(double t) {
            return t;
        }
    },
    SineIn {
        @Override
        public double apply(double t) {
            return 1.0 - Math.cos(t * Math.PI / 2.0);
        }
    },
    SineOut {
        @Override
        public double apply(double t) {
            return Math.sin(t * Math.PI / 2.0);
        }
    },
    SineInOut {
        @Override
        public double apply(double t) {
            return -(Math.cos(Math.PI * t) - 1.0) / 2.0;
        }
    },
    QuadIn {
        @Override
        public double apply(double t) {
            return t * t;
        }
    },
    QuadOut {
        @Override
        public double apply(double t) {
            return 1.0 - (1.0 - t) * (1.0 - t);
        }
    },
    QuadInOut {
        @Override
        public double apply(double t) {
            return t < 0.5 ? 2.0 * t * t : 1.0 - Math.pow(-2.0 * t + 2.0, 2.0) / 2.0;
        }
    },
    CubicIn {
        @Override
        public double apply(double t) {
            return t * t * t;
        }
    },
    CubicOut {
        @Override
        public double apply(double t) {
            return 1.0 - Math.pow(1.0 - t, 3.0);
        }
    },
    CubicInOut {
        @Override
        public double apply(double t) {
            return t < 0.5 ? 4.0 * t * t * t : 1.0 - Math.pow(-2.0 * t + 2.0, 3.0) / 2.0;
        }
    },
    ExpoIn {
        @Override
        public double apply(double t) {
            return t <= 0.0 ? 0.0 : Math.pow(2.0, 10.0 * t - 10.0);
        }
    },
    ExpoOut {
        @Override
        public double apply(double t) {
            return t >= 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * t);
        }
    },
    ExpoInOut {
        @Override
        public double apply(double t) {
            if (t <= 0.0) return 0.0;
            if (t >= 1.0) return 1.0;
            return t < 0.5 ? Math.pow(2.0, 20.0 * t - 10.0) / 2.0 : (2.0 - Math.pow(2.0, -20.0 * t + 10.0)) / 2.0;
        }
    },
    BackOut {
        @Override
        public double apply(double t) {
            double c1 = 1.70158;
            double c3 = c1 + 1.0;
            return 1.0 + c3 * Math.pow(t - 1.0, 3.0) + c1 * Math.pow(t - 1.0, 2.0);
        }
    };

    public abstract double apply(double t);

    /** Applies the easing curve to a 0..1 progress value, clamping the input. */
    public double ease(double t) {
        if (t <= 0.0) return 0.0;
        if (t >= 1.0) return 1.0;
        return apply(t);
    }
}
