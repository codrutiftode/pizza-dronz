package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;

public class CoordinateCalculator {
    private final static double LNGLAT_DOUBLE_PRECISION = 100;

    protected double x(LngLat p) {
        return p.lng();
    }

    protected double y(LngLat p) {
        return p.lat();
    }

    /**
     * Tests whether the middle number is between the two outer ones, in any order
     */
    protected boolean isInBetween(double a, double b, double c) {
        return (a < b && b < c) || (c < b && b < a);
    }

    protected boolean isInBetweenEquals(double a, double b, double c) {
        return (a <= b && b <= c) || (c <= b && b <= a);
    }

    /**
     * Rounds a double to a fixed precision
     * @param d number to round
     * @return rounded number
     */
    protected double roundDouble(double d) {
        return Math.round(d * LNGLAT_DOUBLE_PRECISION) / LNGLAT_DOUBLE_PRECISION;
    }

    /**
     * Tests if two doubles are equal when rounded to a fixed precision
     * @param a first double
     * @param b second double
     * @return true if equal, false otherwise
     */
    protected boolean doublesEqual(double a, double b) {
        return roundDouble(a) == roundDouble(b);
    }
}
