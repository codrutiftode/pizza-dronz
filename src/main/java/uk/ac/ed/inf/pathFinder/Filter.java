package uk.ac.ed.inf.pathFinder;

import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Filter implements IFilter<RouteNode<LngLat>> {
    private final NamedRegion[] noFlyZones;
    private final NamedRegion centralArea;

    public Filter(NamedRegion[] noFlyZones, NamedRegion centralArea) {
        this.noFlyZones = noFlyZones;
        this.centralArea = centralArea;
    }

    @Override
    public List<RouteNode<LngLat>> filterNodes(List<RouteNode<LngLat>> possibleMoves, boolean stayInCentral) {
        return possibleMoves.stream().filter(move -> !crossesIntoNoFly(move, stayInCentral)).toList();
    }

    private boolean ccw(LngLat a, LngLat b, LngLat c) {
        return (c.lat() - a.lat()) * (b.lng() - a.lng()) > (b.lat() - a.lat()) * (c.lng() - a.lng());
    }

    private boolean segmentsIntersect(LngLat a, LngLat b, LngLat c, LngLat d) {
        return (ccw(a, c, d) != ccw(b, c, d)) && (ccw(a,b,c) != ccw(a,b,d));
    }

    private boolean crossesIntoNoFly(RouteNode<LngLat> move, boolean stayInCentral) {
        LngLat oldPosition = move.getPreviousNode().getCurrentPosition();
        LngLat newPosition = move.getCurrentPosition();
        List<NamedRegion> noFlyRegions = new ArrayList<>(Arrays.stream(noFlyZones).toList());
        if (stayInCentral) noFlyRegions.add(centralArea);

        for (NamedRegion region : noFlyRegions) {
            for (int i = 0; i + 1 < region.vertices().length; i++) {
                int j = i + 1;
                boolean intersect = segmentsIntersect(oldPosition, newPosition, region.vertices()[i], region.vertices()[j]);
                if (intersect) return true;
            }
        }
        return false;
    }
}
