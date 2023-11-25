package uk.ac.ed.inf.pathFinder;

import uk.ac.ed.inf.LngLatHandler;
import uk.ac.ed.inf.TimeKeeper;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;

import java.util.ArrayList;
import java.util.List;

public class RouteNode<PositionT> implements Comparable<RouteNode<PositionT>> {
    protected double costSoFar;
    protected PositionT currentPosition;
    protected RouteNode<PositionT> previousNode;
    protected int previousMove;
    private long timestamp = -1;
    protected PositionT endPoint;
    protected IHeuristic<PositionT> heuristic;

    private INavigator<PositionT> navigator;

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

    public List<RouteNode<PositionT>> getNextMoves() {
        List<RouteNode<PositionT>> nextMoves = new ArrayList<>();
        final double costOfOneMove = SystemConstants.DRONE_MOVE_DISTANCE;
        int circleDivisions = 16;
        double angleIncrement = 360.0 / circleDivisions;
        for (int i = 0; i < circleDivisions; i++) {
            if ((i + 8) % 16 == previousMove) continue;
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

    public void stampTime() {
        this.timestamp = TimeKeeper.getTimeKeeper().getTime();
    }

    public double computeCost() {
        double h = heuristic.heuristicCost(getCurrentPosition(), endPoint);
        double g = getCostSoFar();
        return h * 1.05 + g;
    }

    @Override
    public int compareTo(RouteNode<PositionT> otherNode) {
        double result = computeCost() - otherNode.computeCost();
        if (result > 0) return 1;
        else if (result < 0) return -1;
        return 0;
    }
}