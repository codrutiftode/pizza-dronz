package uk.ac.ed.inf.pathFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class AStar<PositionT> {
    private IHeuristic<PositionT> heuristic;
    private IFilter<RouteNode<PositionT>> filter;
    private INavigator<PositionT> navigator;

    public void setNavigator(INavigator<PositionT> navigator) {
        this.navigator = navigator;
    }
    public void setHeuristic(IHeuristic<PositionT> h) {
        this.heuristic = h;
    }

    public void setFilter(IFilter<RouteNode<PositionT>> f) {
        this.filter = f;
    }

    public List<FlightMove<PositionT>> run(PositionT startPoint, PositionT endPoint) {
        return run(startPoint, endPoint, false);
    }

    public List<FlightMove<PositionT>> run(PositionT startPoint, PositionT endPoint, boolean stayInCentral) {
        PriorityQueue<RouteNode<PositionT>> frontier = new PriorityQueue<>();
        RouteNode<PositionT> currentNode = new RouteNode<>(0, startPoint, null, -1, endPoint, heuristic, navigator);
        int counter = 0;
        while (!navigator.isCloseTo(currentNode.getCurrentPosition(), endPoint)) {
            List<RouteNode<PositionT>> nextMoves = filter.filterNodes(currentNode.getNextMoves(), stayInCentral);
            currentNode.stampTime();
            frontier.addAll(nextMoves);
            currentNode = frontier.poll();
            if (currentNode == null) { // TODO: consider this case
                break;
            }
            if (counter < 90000) {
                counter += 1;
            }
            else {
                System.out.println("e");
                break;
            }
        }

        // Construct path from saved graph traversal nodes
        ArrayList<RouteNode<PositionT>> nodesList = new ArrayList<>();
        while (currentNode.getPreviousNode() != null) {
            nodesList.add(currentNode);
            currentNode = currentNode.getPreviousNode();
        }
        Collections.reverse(nodesList);

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
}
