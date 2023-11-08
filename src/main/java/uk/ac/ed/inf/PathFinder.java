package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import java.util.Arrays;

public class PathFinder {
    private final LngLat dropOffPoint;
    private final NamedRegion[] noFlyZones;
    private final NamedRegion centralArea;

    public PathFinder(NamedRegion[] noFlyZones, NamedRegion centralArea, LngLat dropOffPoint) {
        this.noFlyZones = noFlyZones;
        this.centralArea = centralArea;
        this.dropOffPoint = dropOffPoint;
    }
    public LngLat[] computePath(LngLat targetLocation) {
        LngLat[] pathToRestaurant = findPathBetween(dropOffPoint, targetLocation, null);
        LngLat[] pathToCentralArea = findPathToCentralArea(targetLocation);
        LngLat entranceInCentralArea = pathToCentralArea[pathToCentralArea.length - 1];
        LngLat[] pathBackToDropOff = findPathBetween(entranceInCentralArea, dropOffPoint, centralArea);
        return concatPaths(
                pathToRestaurant,
                new LngLat[]{ targetLocation },
                pathToCentralArea,
                pathBackToDropOff,
                new LngLat[]{ dropOffPoint }
        );
    }

    private LngLat[] concatPaths(LngLat[]... paths) {
        int totalLength = Arrays.stream(paths).mapToInt(path -> path.length).sum();
        LngLat[] totalPath = new LngLat[totalLength];
        int i = 0;
        for (LngLat[] path : paths) {
            for (LngLat lngLat : path) {
                totalPath[i] = lngLat;
                i++;
            }
        }
        return totalPath;
    }

    private LngLat[] findPathToCentralArea(LngLat startPoint) {
        return new LngLat[]{new LngLat(1.5, 1.5), new LngLat(1.6, 1.6)};
    }

    private LngLat[] findPathBetween(LngLat startPoint, LngLat endPoint, NamedRegion maxRegion) {
        return new LngLat[]{startPoint, endPoint};
    }
}