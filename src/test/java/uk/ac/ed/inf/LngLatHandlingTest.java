package uk.ac.ed.inf;

import junit.framework.TestCase;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

public class LngLatHandlingTest extends TestCase {
    public void testDistanceTo()
    {
        LngLatHandling handler = new LngLatHandling();
        double distance = handler.distanceTo(new LngLat(3, 2), new LngLat(4, 5));
        assertEquals(distance, Math.sqrt(10));
    }

    public void testIsCloseTo() {
        LngLatHandling handler = new LngLatHandling();
        boolean isClose = handler.isCloseTo(new LngLat(10, 10), new LngLat(10.00012, 10));
        boolean isClose2 = handler.isCloseTo(new LngLat(10, 10), new LngLat(10.00016, 10));
        assertTrue(isClose);
        assertFalse(isClose2);
    }

    public void testIsInRegionSimple() {
        LngLatHandling handler = new LngLatHandling();
        LngLat[] vertices = new LngLat[]{
                new LngLat(0, 0),
                new LngLat(0, 6),
                new LngLat(6, 0),
                new LngLat(6, 6)
        };
        NamedRegion region = new NamedRegion("region", vertices);
        boolean isInside = handler.isInRegion(new LngLat(3, 3), region);
        assertTrue(isInside);
    }

    public void testIsInRegionOnBorder() {
        LngLatHandling handler = new LngLatHandling();
        LngLat[] vertices = new LngLat[]{
                new LngLat(0, 0),
                new LngLat(6, 0),
                new LngLat(6, 6),
                new LngLat(0, 6)
        };
        NamedRegion region = new NamedRegion("region", vertices);
        boolean isInside = handler.isInRegion(new LngLat(3, 6), region);
        assertTrue(isInside);
    }

    public void testIsInRegionOutside() {
        LngLatHandling handler = new LngLatHandling();
        LngLat[] vertices = new LngLat[]{
                new LngLat(0, 0),
                new LngLat(6, 0),
                new LngLat(6, 6),
                new LngLat(0, 6)
        };
        NamedRegion region = new NamedRegion("region", vertices);
        boolean isInside = handler.isInRegion(new LngLat(-1, 3), region);
        assertFalse(isInside);
    }

    public void testIsInRegionComplexPolygon1() {
        LngLatHandling handler = new LngLatHandling();
        LngLat[] vertices = new LngLat[]{
                new LngLat(0, 0),
                new LngLat(6, 0),
                new LngLat(6, 2),
                new LngLat(4, 2),
                new LngLat(4, 5),
                new LngLat(3, 5),
                new LngLat(3, 2),
                new LngLat(0, 2),
        };
        NamedRegion region = new NamedRegion("region", vertices);
        boolean isInside = handler.isInRegion(new LngLat(3, 4), region);
        assertTrue(isInside);
    }

    public void testIsInRegionComplexPolygon2() {
        LngLatHandling handler = new LngLatHandling();
        LngLat[] vertices = new LngLat[]{
                new LngLat(0, 0),
                new LngLat(2, 3),
                new LngLat(1, 3),
                new LngLat(0, 1),
                new LngLat(-1, 3),
                new LngLat(-2, 3),
        };
        NamedRegion region = new NamedRegion("region", vertices);
        boolean isInside = handler.isInRegion(new LngLat(0, 2), region);
        assertFalse(isInside);
    }
}
