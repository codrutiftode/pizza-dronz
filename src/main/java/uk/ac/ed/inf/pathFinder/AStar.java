package uk.ac.ed.inf.pathFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Instance of the A* search algorithm
 * @param <PositionT> Generic type for positions in the plane used by A*
 */
public class AStar<PositionT> {
    private IHeuristic<PositionT> heuristic;
    private IFilter<RouteNode<PositionT>> filter;
    private INavigator<PositionT> navigator;

    /** Maximum number of nodes that can be expanded from the frontier. For no limit, set to -1 **/
    private int frontierExpansionLimit = -1;

    public void setNavigator(INavigator<PositionT> navigator) {
        this.navigator = navigator;
    }
    public void setHeuristic(IHeuristic<PositionT> h) {
        this.heuristic = h;
    }

    public void setFilter(IFilter<RouteNode<PositionT>> f) {
        this.filter = f;
    }

    public void setFrontierExpansionLimit(int newLimit) {
        this.frontierExpansionLimit = newLimit;
    }

    /**
     * Runs A* to find a list of moves from start to end
     * @param startPoint the starting point
     * @param endPoint the point to end on
     * @param stayInCentral whether to stay inside the central area during travel
     * @return A list of moves to get the drone from start to end
     */
    public List<FlightMove<PositionT>> run(PositionT startPoint, PositionT endPoint, boolean stayInCentral) {
        RouteNode<PositionT> currentNode = expandFrontier(startPoint, endPoint, stayInCentral);
        if (currentNode == null) return null; // No path found
        List<RouteNode<PositionT>> nodes = getNodesInOrder(currentNode);
        return getPathFromNodes(nodes);
    }

    /**
     * Repeatedly expands frontier until goal is reached
     * @param startPoint the point to start from
     * @param endPoint the point to arrive at
     * @param stayInCentral whether the program should stay within the central area
     * @return the final node after goal is reached
     */
    private RouteNode<PositionT> expandFrontier(PositionT startPoint, PositionT endPoint, boolean stayInCentral) {
        PriorityQueue<RouteNode<PositionT>> frontier = new PriorityQueue<>();
        RouteNode<PositionT> currentNode = getInitialNode(startPoint, endPoint);
        int noExpandedNodes = 0;

        while (!navigator.isCloseTo(currentNode.getCurrentPosition(), endPoint)) {
            List<RouteNode<PositionT>> nextMoves = filter.filterNodes(currentNode.getNextMoves(), stayInCentral);
            currentNode.stampTime();
            frontier.addAll(nextMoves);

            currentNode = frontier.poll();
            if (currentNode == null) { // Frontier empty, no path possible
                return null;
            }

            // Break if too many nodes have been expanded
            noExpandedNodes += 1;
            if (frontierExpansionLimit != -1 && noExpandedNodes >= frontierExpansionLimit) break;
        }
        return currentNode;
    }

    /**
     * Order the ancestors of the current node linearly and increasingly
     * @param currentNode the current node
     * @return the path formed of ancestors, in order
     */
    private List<RouteNode<PositionT>> getNodesInOrder(RouteNode<PositionT> currentNode) {
        ArrayList<RouteNode<PositionT>> nodesList = new ArrayList<>();
        while (currentNode.getPreviousNode() != null) {
            nodesList.add(currentNode);
            currentNode = currentNode.getPreviousNode();
        }
        Collections.reverse(nodesList);
        return nodesList;
    }

    /**
     * Turns a series of nodes into a series of flight moves
     * @param nodesList the series of nodes
     * @return the resulting list of flight moves
     */
    private List<FlightMove<PositionT>> getPathFromNodes(List<RouteNode<PositionT>> nodesList) {
        ArrayList<FlightMove<PositionT>> moveList = new ArrayList<>();
        for (int i = 0; i + 1< nodesList.size(); i++) {
            RouteNode<PositionT> node = nodesList.get(i);
            RouteNode<PositionT> nextNode = nodesList.get(i + 1);
            FlightMove<PositionT> move = new FlightMove<>(node.getCurrentPosition(),
                    nextNode.getCurrentPosition(),
                    node.getPreviousMove() * 22.5,
                    node.getTimestamp()
            );
            moveList.add(move);
        }
        return moveList;
    }

    /**
     * Constructs the first node to be used by A*
     */
    private RouteNode<PositionT> getInitialNode(PositionT startPoint, PositionT endPoint) {
        return new RouteNode<>(0,
                startPoint,
                null,
                -1,
                endPoint,
                heuristic,
                navigator
        );
    }
}
