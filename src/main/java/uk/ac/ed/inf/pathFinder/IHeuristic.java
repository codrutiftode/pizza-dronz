package uk.ac.ed.inf.pathFinder;

/**
 * Implements the Heuristic interface
 * @param <PositionT> the type of position in the plane used by A*
 */
public interface IHeuristic<PositionT> {
    double heuristicCost(PositionT start, PositionT end);
}
