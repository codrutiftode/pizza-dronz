package uk.ac.ed.inf;

import junit.framework.TestCase;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

import java.util.Arrays;

public class PathFinderTest extends TestCase {
    public void test1() {
        LngLat dropOff = new LngLat(5, 5);
        NamedRegion[] noFlyZones = new NamedRegion[]{
                new NamedRegion("no-fly-1", new LngLat[]{
                        new LngLat(0, 0),
                        new LngLat(0, 3),
                        new LngLat(3, 3)
                })
        };
        NamedRegion centralArea = new NamedRegion("central-area",
                new LngLat[]{
                        new LngLat(0, 0),
                        new LngLat(0, 10),
                        new LngLat(10, 10),
                        new LngLat(10, 0)
                });
        LngLat targetLocation = new LngLat(7, 7);
        PathFinder pathFinder = new PathFinder(noFlyZones, centralArea,dropOff);
        LngLat[] path = pathFinder.computePath(dropOff, targetLocation);
        System.out.println(Arrays.toString(path));
    }
}
