package uk.ac.ed.inf.coordinates;

import uk.ac.ed.inf.CustomConstants;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.pathFinder.INavigator;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for managing coordinates
 */
public class LngLatHandler extends CoordinateCalculator implements uk.ac.ed.inf.ilp.interfaces.LngLatHandling, INavigator<LngLat> {
    @Override
    public double distanceTo(LngLat startPosition, LngLat endPosition) {
        return Math.sqrt(Math.pow(y(endPosition) - y(startPosition), 2) + Math.pow(x(endPosition) - x(startPosition), 2));
    }

    @Override
    public boolean isCloseTo(LngLat startPosition, LngLat otherPosition) {
        return this.distanceTo(startPosition, otherPosition) < SystemConstants.DRONE_IS_CLOSE_DISTANCE;
    }

    /**
     * Checks if a point is within a region
     * @param position the point to consider
     * @param region the polygon that the point should be in
     * @return true if the point is inside the polygon, and false otherwise
     */
    @Override
    public boolean isInRegion(LngLat position, NamedRegion region) {
        return new PointInPoly(position, region).isInside();
    }

    @Override
    public LngLat nextPosition(LngLat startPosition, double angle) {
        return new LngLat(
                startPosition.lng() + SystemConstants.DRONE_MOVE_DISTANCE * Math.cos(angle),
                startPosition.lat() + SystemConstants.DRONE_MOVE_DISTANCE * Math.sin(angle)
        );
    }

    /**
     * Projects a point on the line connecting two other points.
     * @param C the point to project
     * @param A first point on the line
     * @param B second point on the line
     * @return the projection of C on AB
     */
    public LngLat projectOnLine(LngLat C, LngLat A, LngLat B) {
        double x_D, y_D;
        double t1 = x(B) - x(A);
        double t2 = y(B) - y(A);
        if (t1 == 0) { // Vertical line
            x_D = x(A);
            y_D = y(C);
        }
        else {
            double m_AB = t2 / t1;
            y_D = (y(A) + m_AB * x(C) + m_AB * m_AB * y(C) - m_AB * x(A)) / (1 + m_AB * m_AB);
            x_D = (x(C) * t1 - y_D * t2 + y(C) * t2) / t1;
        }
        return new LngLat(x_D, y_D);
    }

    /**
     * Tests if a given point lies on a segment
     * @param D the point to test
     * @param A one end of the segment
     * @param B the other end of the segment
     * @return true if D is on segment [AB], and false otherwise
     */
    public boolean isPointOnSegment(LngLat D, LngLat A, LngLat B) {
        boolean condition1 = isInBetweenEquals(x(A), x(D), x(B));
        boolean condition2 = isInBetweenEquals(y(A), y(D), y(B));
        boolean condition3 = doublesEqual((x(B) - x(A)) * (y(D) - y(A)), (x(D) - x(A)) * (y(B) - y(A)));
        return condition1 && condition2 && condition3;
    }

    /**
     * Compute the projection of a given point on every segment
     * @param p the given point
     * @param vertices an array of segments
     * @return a list of all valid projections on the segments
     */
    public List<LngLat> projectPointOnSegments(LngLat p, LngLat[] vertices) {
        ArrayList<LngLat> projections = new ArrayList<>();
        for (int i = 0; i < vertices.length; i++) {
            LngLat vertex1 = vertices[i];
            LngLat vertex2 = vertices[(i + 1) % vertices.length];
            LngLat projection = projectOnLine(p, vertex1, vertex2);
            if (isPointOnSegment(projection, vertex1, vertex2)) {
                projections.add(projection);
            }
        }
        return projections;
    }

    /**
     * Takes one move in the closest valid direction that approximates moving in the direction of the "towards" point
     * @param start The starting point
     * @param towards The point to try to move towards
     * @return the position after taking the move
     */
    public LngLat takeOneMove(LngLat start, LngLat towards) {
        double angle = Math.toDegrees(Math.atan2(towards.lat() - start.lat(), towards.lng() - start.lng()));
        double positiveAngle = angle > 0 ? angle : 360 - angle;
        double angleIncrement = 360.0 / CustomConstants.DIRECTIONS_CIRCLE_DIVISIONS;
        double closestAngle = Math.ceil(positiveAngle / angleIncrement) * angleIncrement; // The closest valid angle based on drone restrictions
        return nextPosition(start, Math.toRadians(closestAngle));
    }
}
