package net.pl3x.livemap.util;

/**
 * Some math related stuffs.
 */
public final class Mathf {
    private Mathf() {
    }

    /**
     * Linearly interpolates between {@code a} and {@code b} by {@code t}.
     * <p>
     * The parameter {@code t} is clamped to the range [0, 1].
     * <p>
     * When {@code t} = 0 returns {@code a}.<br>
     * When {@code t} = 1 return {@code b}.<br>
     * When {@code t} = 0.5 returns the midpoint of {@code a} and {@code b}.
     *
     * @param a The start value
     * @param b The end value
     * @param t The interpolation value between the two floats
     * @return The interpolated float result between the two float values
     */
    public static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    /**
     * Determines where a {@code value} lies between two points.
     * <p>
     * The {@code a} and {@code b} values define the start and end of a linear numeric range.
     * The {@code value} parameter you supply represents a value which might lie
     * somewhere within that range. This method calculates where, within the
     * specified range, the {@code value} parameter falls.
     * <p>
     * If the {@code value} parameter is within the range, returns a value between
     * zero and one, proportional to the value's position within the range.
     *
     * @param a The start of the range
     * @param b The end of the range
     * @param t The point within the range you want to calculate
     * @return The value between zero and one, resenting where the {@code value}
     * parameter falls within the range defined by {@code a} and {@code b}
     */
    public static float inverseLerp(float a, float b, float t) {
        return (t - a) / (b - a);
    }

    /**
     * Clamp a value between min and max (inclusive).
     *
     * @param min   Minimum value
     * @param max   Maximum value
     * @param value Value to clamp
     * @return Clamped value between min and max
     */
    public static float clamp(float min, float max, float value) {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Get the square of a number (n * n).
     *
     * @param n The number to square
     * @return The square of the number
     */
    public static double square(double n) {
        return n * n;
    }

    /**
     * Cubic easing out function - decelerating to zero velocity.
     *
     * @param t The time factor, between 0.0 and 1.0
     * @return The value of the function for the given time factor
     * @see <a href="https://easings.net/#easeOutCubic">https://easings.net</a> for example
     */
    public static float easeCubicOut(float t) {
        return 1F + ((t -= 1F) * t * t);
    }

    /**
     * Quintic easing out function - decelerating to zero velocity.
     *
     * @param t The time factor, between 0.0 and 1.0
     * @return The value of the function for the given time factor
     * @see <a href="https://easings.net/#easeOutQuint">https://easings.net</a> for example
     */
    public static float easeQuinticOut(float t) {
        return 1F + ((t -= 1F) * t * t * t * t);
    }
}
