package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.SystemConstants;
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
    public List<FlightMove> computePath(LngLat start, LngLat targetLocation) {
//        // Start timer if not started
        if (!TimeKeeper.getTimeKeeper().isStarted()) {
            TimeKeeper.getTimeKeeper().startKeepingTime();
        }

        List<FlightMove> fullPath;
        List<FlightMove> pathToRestaurant = findPathBetween(this.dropOffPoint, targetLocation, null);
        FlightMove targetHoverMove = getHoverMove(getLastPosition(pathToRestaurant));
        fullPath = new ArrayList<>(pathToRestaurant);
        fullPath.add(targetHoverMove);

        LngLat droneAtTarget = targetHoverMove.getTo();
        if (!navigator.isInCentralArea(droneAtTarget, centralArea)) {
            List<FlightMove> pathToCentralArea = findPathToCentralArea(droneAtTarget);
            List<FlightMove> pathToDropOff = findPathBetween(getLastPosition(pathToCentralArea), dropOffPoint, null);
            FlightMove dropOffHoverMove = getHoverMove(getLastPosition(pathToDropOff));
            fullPath.addAll(pathToCentralArea);
            fullPath.addAll(pathToDropOff);
            fullPath.add(dropOffHoverMove);
        }
        else {
            List<FlightMove> pathFromRestaurant = findPathBetween(droneAtTarget, dropOffPoint, null);
            FlightMove dropOffHoverMove = getHoverMove(getLastPosition(pathFromRestaurant));
            fullPath.addAll(pathFromRestaurant);
            fullPath.add(dropOffHoverMove);
        }
        return fullPath;
    }

    private LngLat getLastPosition(List<FlightMove> moves) {
        return moves.get(moves.size() - 1).getTo();
    }

    private FlightMove getHoverMove(LngLat location) {
        return new FlightMove(location,
                location,
                CustomConstants.ANGLE_WHEN_HOVER,
                TimeKeeper.getTimeKeeper().getTime()
        );
    }

    private List<FlightMove> findPathToCentralArea(LngLat startPoint) {
        LngLat[] caVertices = centralArea.vertices();
        ArrayList<LngLat> projections = new ArrayList<>();
        for (int i = 0; i < caVertices.length; i++) {
            LngLat vertex1 = caVertices[i];
            LngLat vertex2 = caVertices[(i + 1) % caVertices.length];
            LngLat projection = navigator.projectOnLine(startPoint, vertex1, vertex2);
            if (navigator.isPointOnSegment(projection, vertex1, vertex2)) {
                projections.add(projection);
            }
        }

        LngLat closestProjection = null;
        double minDistance = 10000; // TODO: make this nicer
        for (LngLat projection : projections) {
            double distance = navigator.distanceTo(startPoint, projection);
            if (distance < minDistance) {
                minDistance = distance;
                closestProjection = projection;
            }
        }
        if (closestProjection != null) {
            return naiveAStar(startPoint, closestProjection);
        }


        double minDistance2 = 10000; // TODO: make this nicer
        LngLat minDistanceVertex = null;
        for (LngLat caVertex : caVertices) {
            double distanceToVertex = navigator.distanceTo(startPoint, caVertex);
            if (distanceToVertex < minDistance2) {
                minDistance2 = distanceToVertex;
                minDistanceVertex = caVertex;
            }
        }

        return naiveAStar(startPoint, minDistanceVertex);

        // Go through every edge in central area
        // IF the segments from start to edge form two acute angles
        //      Create straight line projection onto segment
        //      A* to projection
        // ELSE
        //      Find the nearest corner of central area
        //      A* to it
    }

    // TODO: implement max region for when back inside central area
    private List<FlightMove> findPathBetween(LngLat startPoint, LngLat endPoint, NamedRegion maxRegion) {
        return naiveAStar(startPoint, endPoint);
    }

    private List<FlightMove> naiveAStar(LngLat startPoint, LngLat endPoint) {
        PriorityQueue<FrontierNode> frontier = new PriorityQueue<>();
        FrontierNode currentNode = new FrontierNode(0, startPoint, null, -1, endPoint);
        int counter = 0;
        List<FrontierNode> chosenNodes = new ArrayList<>();
        while (!navigator.isCloseTo(currentNode.getCurrentPosition(), endPoint)) {
            List<FrontierNode> nextMoves = filterNoFly(currentNode.getNextMoves());
            chosenNodes.add(currentNode);
            FrontierNode eastMove = new FrontierNode(0, navigator.nextPosition(currentNode.getCurrentPosition(), Math.toRadians(180)), null, -1, endPoint);
            FrontierNode slightSouthMove = new FrontierNode(0, navigator.nextPosition(currentNode.getCurrentPosition(), Math.toRadians(202.5)), null, -1, endPoint);
            chosenNodes.add(slightSouthMove);
            System.out.println(currentNode.getPreviousMove());

            currentNode.stampTime();
            frontier.addAll(nextMoves);
            currentNode = frontier.poll();
            if (currentNode == null) { // TODO: consider this case
                break;
            }
            if (counter < 20000) {
                counter += 1;
            }
            else break;
        }

//        return chosenNodes.stream().map(n -> new FlightMove(n.getCurrentPosition(), n.getCurrentPosition(), 0, 0)).toList();

        // Construct path from saved graph traversal nodes
        ArrayList<FrontierNode> nodesList = new ArrayList<>();
        while (currentNode.getPreviousNode() != null) {
            nodesList.add(currentNode);
            currentNode = currentNode.getPreviousNode();
        }
        Collections.reverse(nodesList);

        ArrayList<FlightMove> moveList = new ArrayList<>();
        for (int i = 0; i + 1< nodesList.size(); i++) {
            FrontierNode node = nodesList.get(i);
            FrontierNode nextNode = nodesList.get(i + 1);
            FlightMove move = new FlightMove(node.getCurrentPosition(),
                    nextNode.getCurrentPosition(),
                    node.getPreviousMove() * 22.5,
                    node.getTimestamp()
            );
            moveList.add(move);
        }
        return moveList;
    }

    private List<FrontierNode> filterNoFly(List<FrontierNode> possibleMoves) {
        return possibleMoves.stream().filter(move -> !crossesIntoNoFly(move)).toList();
    }

    private boolean ccw(LngLat a, LngLat b, LngLat c) {
        return (c.lat() - a.lat()) * (b.lng() - a.lng()) > (b.lat() - a.lat()) * (c.lng() - a.lng());
    }

    private boolean segmentsIntersect(LngLat a, LngLat b, LngLat c, LngLat d) {
        return (ccw(a, c, d) != ccw(b, c, d)) && (ccw(a,b,c) != ccw(a,b,d));
    }

    private boolean crossesIntoNoFly(FrontierNode move) {
        LngLat oldPosition = move.getPreviousNode().getCurrentPosition();
        LngLat newPosition = move.getCurrentPosition();
        for (NamedRegion region : noFlyZones) {
            for (int i = 0; i + 1< region.vertices().length; i++) {
                int j = i + 1;
                boolean intersect = segmentsIntersect(oldPosition, newPosition, region.vertices()[i], region.vertices()[j]);
                if (intersect) return true;
            }
        }
        return false;
    }

    private boolean isNoFlyPosition(LngLat position) {
        for (NamedRegion noFlyZone : noFlyZones) {
            if (navigator.isInRegion(position, noFlyZone)) {
                return true;
            }
        }
        return false;
    }

    private int pickLowestCost(List<FrontierNode> frontier, LngLat endPoint) {
        double minCost = 100000; // TODO: make this reasonable
        int lowestNodeIndex = 0;
        for (int i = 0; i < frontier.size(); i++) {
            FrontierNode node = frontier.get(i);
            double h = heuristicCost(node.getCurrentPosition(), endPoint);
            double g = node.getCostSoFar();
            double cost = h + g;
            if (cost < minCost) {
                minCost = cost;
                lowestNodeIndex = i;
            }
        }
        return lowestNodeIndex;
    }

    private double heuristicCost(LngLat start, LngLat end) {
        return navigator.distanceTo(start, end);
    }

    private class FrontierNode implements Comparable<FrontierNode> {
        private double costSoFar;
        private LngLat currentPosition;
        private FrontierNode previousNode;
        private int previousMove;
        private long timestamp = -1;
        private LngLat endPoint;

        public FrontierNode(double costSoFar, LngLat currentPosition, FrontierNode previousNode, int previousMove, LngLat endPoint) {
            this.costSoFar = costSoFar;
            this.currentPosition = currentPosition;
            this.previousNode = previousNode;
            this.previousMove = previousMove;
            this.endPoint = endPoint;
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

        public LngLat getCurrentPosition() {
            return currentPosition;
        }

        public FrontierNode getPreviousNode() {
            return previousNode;
        }

        public List<FrontierNode> getNextMoves() {
            List<FrontierNode> nextMoves = new ArrayList<>();
            final double costOfOneMove = SystemConstants.DRONE_MOVE_DISTANCE;
            int circleDivisions = 16;
            double angleIncrement = 360.0 / circleDivisions;
            for (int i = 0; i < circleDivisions; i++) {
                if ((i + 8) % 16 == previousMove) continue;
                LngLat nextPosition = navigator.nextPosition(currentPosition, Math.toRadians(i * angleIncrement));
                FrontierNode newNode = new FrontierNode(costSoFar + costOfOneMove, nextPosition, this, i, endPoint);
                nextMoves.add(newNode);
            }
            return nextMoves;
        }

        public void stampTime() {
            this.timestamp = TimeKeeper.getTimeKeeper().getTime();
        }

        public double computeCost() {
            double h = heuristicCost(getCurrentPosition(), endPoint);
            double g = getCostSoFar();
            return h * 1.05 + g;
        }

        @Override
        public int compareTo(FrontierNode otherNode) {
            double result = computeCost() - otherNode.computeCost();
            if (result > 0) return 1;
            else if (result < 0) return -1;
            return 0;
        }
    }
}