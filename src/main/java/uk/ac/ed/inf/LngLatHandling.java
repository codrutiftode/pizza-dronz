package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

import java.util.Arrays;

public class LngLatHandling implements uk.ac.ed.inf.ilp.interfaces.LngLatHandling {

    private final static double LNGLAT_DOUBLE_PRECISION = 100;

    @Override
    public double distanceTo(LngLat startPosition, LngLat endPosition) {
        return Math.sqrt(Math.pow(endPosition.lat() - startPosition.lat(), 2) +
                        Math.pow(endPosition.lng() - startPosition.lng(), 2)
        );
    }

    @Override
    public boolean isCloseTo(LngLat startPosition, LngLat otherPosition) {
        return this.distanceTo(startPosition, otherPosition) < SystemConstants.DRONE_IS_CLOSE_DISTANCE;
    }

    /**
     * Tests whether the middle number is between the two outer ones, in any order
     */
    private boolean isInBetween(double a, double b, double c) {
        return (a < b && b < c) || (c < b && b < a);
    }

    /**
     * Projects a vertex on a given segment
     * Note: assume the segment is neither vertical nor horizontal
     * @param lat the latitude of the target vertex
     * @param point1 one end of the segment
     * @param point2 the other end of the segment
     * @return the longitude of the projected point on the segment
     */
    private double projectOnSegment(double lat, LngLat point1, LngLat point2) {
        double slopeOfSegment = (point2.lat() - point1.lat()) / (point2.lng() - point1.lng());
        // From equation of a straight line
        return (lat - point1.lat() + slopeOfSegment * point1.lng()) / slopeOfSegment;
    }

    /**
     * Computes if ray intersects given segment
     * @param currentPos position to start shooting the ray from, towards the right
     * @param point1 one end of the segment
     * @param point2 the other end of the segment
     * @return true if ray intersects segment, false otherwise
     */
    private boolean rayIntersectsSegment(LngLat currentPos, LngLat point1, LngLat point2) {
        if (roundDouble(point1.lng()) == roundDouble(point2.lng())) {
            return point1.lng() >= currentPos.lng() && isInBetween(point1.lat(), currentPos.lat(), point2.lat());
        }
        try {
            double xCoord = projectOnSegment(currentPos.lat(), point1, point2);
            return currentPos.lng() < xCoord && isInBetween(point1.lng(), xCoord, point2.lng());
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Rounds a double to a fixed precision
     * @param d number to round
     * @return rounded number
     */
    private double roundDouble(double d) {
        return Math.round(d * LNGLAT_DOUBLE_PRECISION) / LNGLAT_DOUBLE_PRECISION;
    }

    /**
     * Checks if a given position lands on a given segment
     * @param currentPos the position to check
     * @param point1 one end of the segment
     * @param point2 the other end of the segment
     * @return true if the position is geometrically on the segment, false otherwise
     */
    private boolean isOnSegment(LngLat currentPos, LngLat point1, LngLat point2) {
        if (roundDouble(point1.lat()) == roundDouble(point2.lat())) { // If segment is horizontal
            return roundDouble(currentPos.lat()) == roundDouble(point1.lat()) && isInBetween(point1.lng(), currentPos.lng(), point2.lng());
        }
        else if (roundDouble(point1.lng()) == roundDouble(point2.lng())) { // If segment is vertical
            return roundDouble(currentPos.lng()) == roundDouble(point1.lng()) && isInBetween(point1.lat(), currentPos.lat(), point2.lat());
        }
        else { // If segment is neither horizontal nor vertical
            double projectedLng = projectOnSegment(currentPos.lat(), point1, point2);
            return roundDouble(currentPos.lng()) == roundDouble(projectedLng) && isInBetween(point1.lng(), projectedLng, point2.lng());
        }
    }

    /**
     * Rotates a vertex around the origin (0, 0), using the standard rotation matrix
     * @param vertex the vertex to rotate
     * @param angle the angle between the old position vector and new position vector
     * @return the new rotated vertex
     */
    private LngLat rotateVertex(LngLat vertex, double angle) {
        return new LngLat(
                vertex.lng() * Math.cos(angle) - vertex.lat() * Math.sin(angle),
                vertex.lng() * Math.sin(angle) + vertex.lat() * Math.cos(angle)
        );
    }

    /**
     * Rotates a plane of multiple vertices around the origin (0, 0)
     * @param vertices the vertices making up the plane
     * @param angle the angle of rotation
     * @return an array of rotated vertices
     */
    private LngLat[] rotatePlane(LngLat[] vertices, double angle) {
        return Arrays.stream(vertices).map((LngLat vertex) -> rotateVertex(vertex, angle)).toArray(LngLat[]::new);
    }

    @Override
    public boolean isInRegion(LngLat position, NamedRegion region) {
        // Use Ray Casting algorithm
        // If y coordinate matches one or more vertices, rotate the plane by small degree
        LngLat[] vertices = region.vertices();
        LngLat currentPosition = position;
        while (Arrays.stream(vertices).anyMatch((LngLat vertex) -> roundDouble(vertex.lat()) == roundDouble(position.lat()))) {
            vertices = rotatePlane(vertices, Math.toRadians(1));
            currentPosition = rotateVertex(position, Math.toRadians(1));
        }

        // Count intersections between a ray shooting to the right and each segment in 'region'
        int intersectionCount = 0;
        for (int i = 0; i < vertices.length; i++) {
            LngLat nextVertex = vertices[(i + 1) % vertices.length]; // Loop back to start
            if (isOnSegment(currentPosition, vertices[i], nextVertex)) {
                return true;
            }
            else if (rayIntersectsSegment(currentPosition, vertices[i], nextVertex)) {
                intersectionCount++;
            }
        }

        // If odd number of intersections, vertex is inside polygon
        return intersectionCount % 2 == 1;
    }

    @Override
    public LngLat nextPosition(LngLat startPosition, double angle) {
        return new LngLat(
                startPosition.lng() + SystemConstants.DRONE_MOVE_DISTANCE * Math.cos(angle),
                startPosition.lat() + SystemConstants.DRONE_MOVE_DISTANCE * Math.sin(angle)
        );
    }
}