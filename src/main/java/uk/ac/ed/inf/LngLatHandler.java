package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.pathFinder.INavigator;

import java.util.ArrayList;
import java.util.List;

public class LngLatHandler extends CoordinateCalculator implements uk.ac.ed.inf.ilp.interfaces.LngLatHandling, INavigator<LngLat> {
    @Override
    public double distanceTo(LngLat startPosition, LngLat endPosition) {
        return Math.sqrt(Math.pow(y(endPosition) - y(startPosition), 2) + Math.pow(x(endPosition) - x(startPosition), 2));
    }

    @Override
    public boolean isCloseTo(LngLat startPosition, LngLat otherPosition) {
        return this.distanceTo(startPosition, otherPosition) < SystemConstants.DRONE_IS_CLOSE_DISTANCE;
    }

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

    public boolean isPointOnSegment(LngLat D, LngLat A, LngLat B) {
        boolean condition1 = isInBetweenEquals(x(A), x(D), x(B));
        boolean condition2 = isInBetweenEquals(y(A), y(D), y(B));
        boolean condition3 = doublesEqual((x(B) - x(A)) * (y(D) - y(A)), (x(D) - x(A)) * (y(B) - y(A)));
        return condition1 && condition2 && condition3;
    }

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
     * Tests if a position is within a region's polygon, using ray casting.
     * This draws a straight line to the right and counts the times it intersects the polygon
     * If it is odd, the position is within the region, otherwise it is outside.
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
}
