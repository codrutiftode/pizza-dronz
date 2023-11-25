package uk.ac.ed.inf.pathFinder;

import uk.ac.ed.inf.CustomConstants;
import uk.ac.ed.inf.coordinates.LngLatHandler;
import uk.ac.ed.inf.TimeKeeper;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import java.util.*;

public class PathFinder {
    private final LngLat dropOffPoint;
    private final NamedRegion[] noFlyZones;
    private final NamedRegion centralArea;

    private final LngLatHandler navigator;

    public PathFinder(NamedRegion[] noFlyZones, NamedRegion centralArea, LngLat dropOffPoint) {
        this.noFlyZones = noFlyZones;
        this.centralArea = centralArea;
        this.dropOffPoint = dropOffPoint;
        this.navigator = new LngLatHandler();
    }
    public List<FlightMove<LngLat>> computePath(LngLat start, LngLat targetLocation) {
        // Start timer if not started
        if (!TimeKeeper.getTimeKeeper().isStarted()) {
            TimeKeeper.getTimeKeeper().startKeepingTime();
        }

        List<FlightMove<LngLat>> fullPath;
        List<FlightMove<LngLat>> pathToRestaurant = findPathBetween(this.dropOffPoint, targetLocation, false);
        FlightMove<LngLat> targetHoverMove = getHoverMove(getLastPosition(pathToRestaurant));
        fullPath = new ArrayList<>(pathToRestaurant);
        fullPath.add(targetHoverMove);

        LngLat droneAtTarget = targetHoverMove.getTo();
        if (!navigator.isInCentralArea(droneAtTarget, centralArea)) {
            List<FlightMove<LngLat>> pathToCentralArea = findPathToCentralArea(droneAtTarget);
            List<FlightMove<LngLat>> pathToDropOff = findPathBetween(getLastPosition(pathToCentralArea), dropOffPoint, false); // TODO: this needs to be true
            FlightMove<LngLat> dropOffHoverMove = getHoverMove(getLastPosition(pathToDropOff));
            fullPath.addAll(pathToCentralArea);
            fullPath.addAll(pathToDropOff);
            fullPath.add(dropOffHoverMove);
        }
        else {
            List<FlightMove<LngLat>> pathFromRestaurant = findPathBetween(droneAtTarget, dropOffPoint, false);
            FlightMove<LngLat> dropOffHoverMove = getHoverMove(getLastPosition(pathFromRestaurant));
            fullPath.addAll(pathFromRestaurant);
            fullPath.add(dropOffHoverMove);
        }
        return fullPath;
    }

    private LngLat getLastPosition(List<FlightMove<LngLat>> moves) {
        return moves.get(moves.size() - 1).getTo();
    }

    private FlightMove<LngLat> getHoverMove(LngLat location) {
        return new FlightMove<>(location,
                location,
                CustomConstants.ANGLE_WHEN_HOVER,
                TimeKeeper.getTimeKeeper().getTime()
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

    private List<FlightMove<LngLat>> findPathToCentralArea(LngLat startPoint) {
        LngLat[] caVertices = centralArea.vertices();
        List<LngLat> projections = navigator.projectPointOnSegments(startPoint, caVertices);
        LngLat closestProjection = findClosest(startPoint, projections);
        if (closestProjection != null) {
            return findPathBetween(startPoint, closestProjection, false);
        }

        // If there is no projection, the closest point is a central area corner
        LngLat closestVertex = findClosest(startPoint, Arrays.stream(caVertices).toList());
        return findPathBetween(startPoint, closestVertex, false);
    }

    private List<FlightMove<LngLat>> findPathBetween(LngLat startPoint, LngLat endPoint, boolean stayInCentral) {
        AStar<LngLat> aStar = new AStar<>();
        Heuristic h = new Heuristic(navigator);
        Filter f = new Filter(noFlyZones, centralArea);
        aStar.setHeuristic(h);
        aStar.setFilter(f);
        aStar.setNavigator(navigator);
        return aStar.run(startPoint, endPoint, stayInCentral);
    }
}