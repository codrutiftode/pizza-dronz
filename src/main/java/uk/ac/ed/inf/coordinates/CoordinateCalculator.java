package uk.ac.ed.inf.coordinates;

import uk.ac.ed.inf.ilp.data.LngLat;

/**
 * Makes complicated geometry formulas more concise and clean.
 */
public class CoordinateCalculator {
    private final static double LNGLAT_DOUBLE_PRECISION = 1.0E5;

    /**
     * Extracts the longitude.
     * @param p a given coordinate pair
     * @return the longitude
     */
    protected double x(LngLat p) {
        return p.lng();
    }

    /**
     * Extracts the latitude.
     * @param p a given coordinate pair
     * @return the latitude
     */
    protected double y(LngLat p) {
        return p.lat();
    }

    /**
     * Tests whether the middle number is between the two outer ones, in any order.
     */
    protected boolean isInBetween(double a, double b, double c) {
        return (a < b && b < c) || (c < b && b < a);
    }

    protected boolean isInBetweenEquals(double a, double b, double c) {
        return (a <= b && b <= c) || (c <= b && b <= a);
    }

    /**
     * Rounds a double to a fixed precision.
     * @param d number to round
     * @return rounded number.
     */
    protected double roundDouble(double d) {
        return Math.round(d * LNGLAT_DOUBLE_PRECISION) / LNGLAT_DOUBLE_PRECISION;
    }

    /**
     * Tests if two doubles are equal when rounded to a fixed precision.
     * @param a first double
     * @param b second double
     * @return true if equal, false otherwise.
     */
    protected boolean doublesEqual(double a, double b) {
        return roundDouble(a) == roundDouble(b);
    }
}
