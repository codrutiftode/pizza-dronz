package uk.ac.ed.inf.pathFinder;

import uk.ac.ed.inf.CustomConstants;
import uk.ac.ed.inf.TimeKeeper;
import uk.ac.ed.inf.ilp.constant.SystemConstants;

import java.util.ArrayList;
import java.util.List;

public class RouteNode<PositionT> implements Comparable<RouteNode<PositionT>> {
    protected final double costSoFar;
    protected final PositionT currentPosition;
    protected final RouteNode<PositionT> previousNode;
    protected final int previousMove;
    private long timestamp = -1;
    protected final PositionT endPoint;
    protected final IHeuristic<PositionT> heuristic;

    private final INavigator<PositionT> navigator;

    public RouteNode(double costSoFar,
                     PositionT currentPosition,
                     RouteNode<PositionT> previousNode,
                     int previousMove,
                     PositionT endPoint,
                     IHeuristic<PositionT> heuristic,
                     INavigator<PositionT> navigator) {
        this.costSoFar = costSoFar;
        this.currentPosition = currentPosition;
        this.previousNode = previousNode;
        this.previousMove = previousMove;
        this.endPoint = endPoint;
        this.navigator = navigator;
        this.heuristic = heuristic;
    }

    /**
     * Get all possible next moves from the current node
     * @return a list of all possible nodes to visit
     */
    public List<RouteNode<PositionT>> getNextMoves() { // TODO: add closed set. Might mean you don't need the reverse move rule
        final double costOfOneMove = SystemConstants.DRONE_MOVE_DISTANCE;
        int circleDivisions = CustomConstants.DIRECTIONS_CIRCLE_DIVISIONS;
        double angleIncrement = 360.0 / circleDivisions;
        List<RouteNode<PositionT>> nextMoves = new ArrayList<>();

        for (int i = 0; i < circleDivisions; i++) {
            // Should not be able to go back via the same move that was just played
            if (getOpposingMove(i, circleDivisions) == previousMove) continue;

            // Create node for next move
            PositionT nextPosition = navigator.nextPosition(currentPosition, Math.toRadians(i * angleIncrement));
            RouteNode<PositionT> newNode = new RouteNode<>(
                    costSoFar + costOfOneMove,
                    nextPosition,
                    this,
                    i,
                    endPoint,
                    heuristic,
                    navigator
            );
            nextMoves.add(newNode);
        }
        return nextMoves;
    }

    private int getOpposingMove(int moveIndex, int circleDivisions) {
        return (moveIndex + (circleDivisions / 2)) % circleDivisions;
    }

    public int getPreviousMove() {
        return this.previousMove;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public double getCostSoFar() {
        return costSoFar;
    }

    public PositionT getCurrentPosition() {
        return currentPosition;
    }

    public RouteNode<PositionT> getPreviousNode() {
        return previousNode;
    }

    /**
     * Records current time of executing this method
     */
    public void stampTime() {
        this.timestamp = TimeKeeper.getTimeKeeper().getTime();
    }

    /**
     * Computes the cost of the current node
     * @return The current node's cost
     */
    public double computeCost() {
        double h = heuristic.heuristicCost(getCurrentPosition(), endPoint);
        double g = getCostSoFar();
        return h * 3 + g;
    }

    @Override
    public int compareTo(RouteNode<PositionT> otherNode) {
        double result = computeCost() - otherNode.computeCost();
        if (result > 0) return 1;
        else if (result < 0) return -1;
        return 0;
    }
}