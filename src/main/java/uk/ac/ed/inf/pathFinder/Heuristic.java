package uk.ac.ed.inf.pathFinder;

import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.interfaces.LngLatHandling;

/**
 * Wrapper around the heuristic function for A*
 */
public class Heuristic implements IHeuristic<LngLat> {
    private final LngLatHandling navigator;

    public Heuristic(LngLatHandling navigator) {
        this.navigator = navigator;
    }

    @Override
    public double heuristicCost(LngLat start, LngLat end) {
        return navigator.distanceTo(start, end);
    }
}
