package uk.ac.ed.inf;

import junit.framework.TestCase;
import uk.ac.ed.inf.coordinates.LngLatHandler;
import uk.ac.ed.inf.ilp.data.LngLat;

public class PointProjectionTest extends TestCase {
    public void test1() {
        LngLatHandler navigator = new LngLatHandler();
        LngLat projection = navigator.projectOnLine(new LngLat(2, 4), new LngLat(1, 1), new LngLat(5, 3));
        System.out.println(projection);
        assertEquals(3.0, projection.lng());
        assertEquals(2.0, projection.lat());
    }

    public void test2() {
        LngLatHandler navigator = new LngLatHandler();
        boolean isOnSegment = navigator.isPointOnSegment(new LngLat(3, 2), new LngLat(1, 1), new LngLat(5, 3));
        assertTrue(isOnSegment);
    }
}
