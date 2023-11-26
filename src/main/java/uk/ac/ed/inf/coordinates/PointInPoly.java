package uk.ac.ed.inf.coordinates;

import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import java.util.Arrays;

/**
 * Tests if a position is within a region's polygon, using ray casting.
 * This draws a straight line to the right and counts the times it intersects the polygon
 * If it is odd, the position is within the region, otherwise it is outside.
 */
public class PointInPoly extends CoordinateCalculator {
    private final LngLat point;
    private final NamedRegion polygon;
    public PointInPoly(LngLat point, NamedRegion polygon) {
        this.point = point;
        this.polygon = polygon;
    }

    /**
     * Projects a vertex on a given segment
     * Note: assume the segment is neither vertical nor horizontal
     * @param lat the latitude of the target vertex
     * @param A one end of the segment
     * @param B the other end of the segment
     * @return the longitude of the projected point on the segment
     */
    private double projectOnSegment(double lat, LngLat A, LngLat B) {
        double slope = (y(B) - y(A)) / (x(B) - x(A));
        return (lat - y(A) + slope * x(A)) / slope; // From equation of a straight line
    }

    private LngLat getCenterOfPolygon(LngLat[] vertices) {
        double centerX = 0, centerY = 0;
        for (LngLat v : vertices) {
            centerX += x(v);
            centerY += y(v);
        }
        return new LngLat(centerX / vertices.length, centerY / vertices.length);
    }

    /**
     * Checks if a given position lands on a given segment
     * @param C the position to check
     * @param A one end of the segment
     * @param B the other end of the segment
     * @return true if the position is geometrically on the segment, false otherwise
     */
    private boolean isOnSegment(LngLat C, LngLat A, LngLat B) {
        if (doublesEqual(y(A), y(B))) { // If segment is horizontal
            return doublesEqual(y(C), y(A)) && isInBetween(x(A), x(C), x(B));
        }
        else if (doublesEqual(x(A), x(B))) { // If segment is vertical
            return doublesEqual(x(C), x(A)) && isInBetween(y(A), y(C), y(B));
        }
        else { // If segment is neither horizontal nor vertical
            double projectedLng = projectOnSegment(y(C), A, B);
            return doublesEqual(x(C), projectedLng) && isInBetween(x(A), projectedLng, x(B));
        }
    }

    /**
     * Computes if ray intersects given segment
     * @param C position to start shooting the ray from, towards the right
     * @param A one end of the segment
     * @param B the other end of the segment
     * @return true if ray intersects segment, false otherwise
     */
    private boolean rayIntersectsSegment(LngLat C, LngLat A, LngLat B) {
        if (doublesEqual(x(A), x(B))) {
            return x(A) >= x(C) && isInBetween(y(A), y(C), y(B));
        }
        try {
            double x_proj = projectOnSegment(y(C), A, B);
            return x(C) < x_proj && isInBetween(x(A), x_proj, x(B));
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Translates a point to a new origin
     * @param P the original point
     * @param origin the point that should be the new origin for P
     * @return the translated point
     */
    private LngLat translateToOrigin(LngLat P, LngLat origin) {
        return new LngLat(x(P) - x(origin), y(P) - y(origin));
    }

    /**
     * Reverses the operation of translateToOrigin()
     * @param P a point
     * @param origin the new origin
     * @return the translated point
     */
    private LngLat translateFromOrigin(LngLat P, LngLat origin) {
        return new LngLat(x(P) + x(origin), y(P) + y(origin));
    }

    /**
     * Rotates a vertex around the given origin, using the standard rotation matrix
     * @param P the vertex to rotate
     * @param angle the angle between the old position vector and new position vector
     * @param origin the origin to rotate the vertex around
     * @return the new rotated vertex
     */
    private LngLat rotateVertex(LngLat P, double angle, LngLat origin) {
        LngLat translatedP = translateToOrigin(P, origin);
        LngLat rotated = new LngLat(
            x(translatedP) * Math.cos(angle) - y(translatedP) * Math.sin(angle),
            x(translatedP) * Math.sin(angle) + y(translatedP) * Math.cos(angle)
        );
        return translateFromOrigin(rotated, origin);
    }

    /**
     * Rotates a plane of multiple vertices around the given origin
     * @param vertices the vertices making up the plane
     * @param angle the angle of rotation
     * @param origin the origin to rotate the vertices around
     * @return an array of rotated vertices
     */
    private LngLat[] rotatePlane(LngLat[] vertices, double angle, LngLat origin) {
        return Arrays.stream(vertices).map((LngLat P) -> rotateVertex(P, angle, origin)).toArray(LngLat[]::new);
    }

    /**
     * Checks if any of the vertices has the same Y coordinate as the point
     * @param vertices an array of vertices
     * @param point a given point
     * @return true if any vertex has the same Y as point, and false otherwise
     */
    private boolean someVertexSameY(LngLat[] vertices, LngLat point) {
        for (LngLat vertex : vertices) {
            if (doublesEqual(y(vertex), y(point))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the point class member is inside the polygon class member
     * @return true if yes, false otherwise
     */
    public boolean isInside() {
        LngLat[] vertices = polygon.vertices();
        LngLat currentPosition = point;

        // If y coordinate matches one or more vertices, rotate the plane by a small degree until it does not
        while (someVertexSameY(vertices, currentPosition)) {
            LngLat origin = getCenterOfPolygon(vertices);
            vertices = rotatePlane(vertices, Math.toRadians(2), origin);
            currentPosition = rotateVertex(point, Math.toRadians(2), origin);
        }

        // Count intersections between a ray shooting to the right and each segment in 'region'
        int intersectionCount = 0;
        for (int i = 0; i < vertices.length; i++) {
            LngLat nextVertex = vertices[(i + 1) % vertices.length]; // Loop back to start if needed
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
