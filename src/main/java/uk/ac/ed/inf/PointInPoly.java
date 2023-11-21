package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

import java.util.Arrays;

public class PointInPoly extends CoordinateCalculator {
    private LngLat point;
    private NamedRegion polygon;
    public PointInPoly(LngLat point, NamedRegion polygon) {
        this.point = point;
        this.polygon = polygon;
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
     * Checks if a given position lands on a given segment
     * @param currentPos the position to check
     * @param point1 one end of the segment
     * @param point2 the other end of the segment
     * @return true if the position is geometrically on the segment, false otherwise
     */
    private boolean isOnSegment(LngLat currentPos, LngLat point1, LngLat point2) {
        if (doublesEqual(point1.lat(), point2.lat())) { // If segment is horizontal
            return doublesEqual(currentPos.lat(), point1.lat())
                    && isInBetween(point1.lng(), currentPos.lng(), point2.lng());
        }
        else if (doublesEqual(point1.lng(), point2.lng())) { // If segment is vertical
            return doublesEqual(currentPos.lng(), point1.lng())
                    && isInBetween(point1.lat(), currentPos.lat(), point2.lat());
        }
        else { // If segment is neither horizontal nor vertical
            double projectedLng = projectOnSegment(currentPos.lat(), point1, point2);
            return doublesEqual(currentPos.lng(), projectedLng)
                    && isInBetween(point1.lng(), projectedLng, point2.lng());
        }
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
            double projectedLng = projectOnSegment(currentPos.lat(), point1, point2);
            return currentPos.lng() < projectedLng && isInBetween(point1.lng(), projectedLng, point2.lng());
        }
        catch (Exception e) {
            return false;
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

    public boolean isInside() {
        // If y coordinate matches one or more vertices, rotate the plane by small degree
        LngLat[] vertices = polygon.vertices();
        LngLat currentPosition = point;
        while (Arrays.stream(vertices).anyMatch((LngLat vertex) -> roundDouble(vertex.lat()) == roundDouble(point.lat()))) {
            vertices = rotatePlane(vertices, Math.toRadians(1));
            currentPosition = rotateVertex(point, Math.toRadians(1));
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
}
