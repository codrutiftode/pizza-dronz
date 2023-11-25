package uk.ac.ed.inf.pathFinder;

public interface IHeuristic<PositionT> {
    public double heuristicCost(PositionT start, PositionT end);
}
