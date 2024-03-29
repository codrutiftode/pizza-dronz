package uk.ac.ed.inf.pathFinder;

import uk.ac.ed.inf.CustomConstants;
import uk.ac.ed.inf.coordinates.LngLatHandler;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import java.util.*;

public class PathFinder {
    private final LngLat dropOffPoint;
    private final NamedRegion[] noFlyZones;
    private final NamedRegion centralArea;

    private final LngLatHandler navigator;
    private final PathCache<FlightMove<LngLat>> pathCache;

    public PathFinder(NamedRegion[] noFlyZones, NamedRegion centralArea, LngLat dropOffPoint) {
        this.noFlyZones = noFlyZones;
        this.centralArea = centralArea;
        this.dropOffPoint = dropOffPoint;
        this.navigator = new LngLatHandler();
        this.pathCache = new PathCache<>();
    }
    public List<FlightMove<LngLat>> computePath(LngLat targetLocation) {
        try {
            if (pathCache.has(targetLocation)) {
                return pathCache.get(targetLocation);
            }
            List<FlightMove<LngLat>> fullPath;
            List<FlightMove<LngLat>> pathToRestaurant = findPathBetween(this.dropOffPoint, targetLocation, false);
            FlightMove<LngLat> targetHoverMove = getHoverMove(getLastPosition(pathToRestaurant));
            fullPath = new ArrayList<>(pathToRestaurant);
            fullPath.add(targetHoverMove);

            LngLat droneAtTarget = targetHoverMove.getTo();
            if (!navigator.isInCentralArea(droneAtTarget, centralArea)) {
                List<FlightMove<LngLat>> pathToCentralArea = findPathToCentralArea(droneAtTarget);
                List<FlightMove<LngLat>> pathToDropOff = findPathBetween(getLastPosition(pathToCentralArea), dropOffPoint, true);
                FlightMove<LngLat> dropOffHoverMove = getHoverMove(getLastPosition(pathToDropOff));
                fullPath.addAll(pathToCentralArea);
                fullPath.addAll(pathToDropOff);
                fullPath.add(dropOffHoverMove);
            } else {
                List<FlightMove<LngLat>> pathFromRestaurant = findPathBetween(droneAtTarget, dropOffPoint, false);
                FlightMove<LngLat> dropOffHoverMove = getHoverMove(getLastPosition(pathFromRestaurant));
                fullPath.addAll(pathFromRestaurant);
                fullPath.add(dropOffHoverMove);
            }

            // Save to cache
            pathCache.cache(targetLocation, fullPath);
            return fullPath;
        }
        catch (Exception e) {
            return null; // No path could be found
        }
    }

    private LngLat getLastPosition(List<FlightMove<LngLat>> moves) {
        return moves.get(moves.size() - 1).getTo();
    }

    private FlightMove<LngLat> getHoverMove(LngLat location) {
        return new FlightMove<>(location,
                location,
                CustomConstants.ANGLE_WHEN_HOVER
        );
    }

    private int toIntComparator(double floatComparator) {
        return floatComparator > 0 ? 1 : floatComparator == 0 ? 0 : -1;
    }

    private LngLat findClosest(LngLat start, List<LngLat> points) {
        return points.stream()
                .min((a, b) -> toIntComparator(navigator.distanceTo(start, a) - navigator.distanceTo(start, b)))
                .orElse(null);
    }

    /**
     * Finds the closest entrance to central area, by finding the closest either projection
     * on a side of central area or central area corner
     * @param startPoint the start point
     * @return the closest entrance
     */
    private LngLat findCentralAreaEntrance(LngLat startPoint) {
        List<LngLat> projections = navigator.projectPointOnSegments(startPoint, centralArea.vertices());
        List<LngLat> possibleEntrances = new ArrayList<>(projections);
        possibleEntrances.addAll(Arrays.stream(centralArea.vertices()).toList());
        return findClosest(startPoint, possibleEntrances);
    }

    /**
     * Find the quickest path from the given point to the central area
     * @param startPoint the point to start from
     * @return a list of flight moves that take the drone from startPoint to inside the central area
     */
    private List<FlightMove<LngLat>> findPathToCentralArea(LngLat startPoint) throws Exception {
        LngLat entrance = findCentralAreaEntrance(startPoint);

        // Take two moves towards the center of central area to ensure arriving inside the central area
        LngLat center = navigator.getCenterOfPolygon(centralArea.vertices());
        LngLat insideCentralArea = navigator.takeOneMove(navigator.takeOneMove(entrance, center), center);
        return findPathBetween(startPoint, insideCentralArea, false);
    }

    /**
     * Finds path between startPoint and endPoint, while potentially staying within the central area.
     * @param startPoint where to start
     * @param endPoint where to end
     * @param stayInCentral whether the drone should stay within the central area
     * @return a list of flight moves that take the drone from start to end
     */
    private List<FlightMove<LngLat>> findPathBetween(LngLat startPoint, LngLat endPoint, boolean stayInCentral) throws Exception {
        AStar<LngLat> aStar = new AStar<>();
        Heuristic h = new Heuristic(navigator);
        Filter f = new Filter(noFlyZones, centralArea);
        aStar.setHeuristic(h);
        aStar.setFilter(f);
        aStar.setNavigator(navigator);
        aStar.setFrontierExpansionLimit(CustomConstants.DEFAULT_FRONTIER_EXPANSION_LIMIT);
        List<FlightMove<LngLat>> result = aStar.run(startPoint, endPoint, stayInCentral);
        if (result == null) throw new Exception("Path cannot be found.");
        return result;
    }
}