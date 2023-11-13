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
    public LngLat[] computePath(LngLat targetLocation) {
        LngLat[] pathToRestaurant = findPathBetween(dropOffPoint, targetLocation, null);
        CustomLogger.getLogger().log("Path to restaurant found.");
        return pathToRestaurant;
    }

    private LngLat[] concatPaths(LngLat[]... paths) {
        int totalLength = Arrays.stream(paths).mapToInt(path -> path.length).sum();
        LngLat[] totalPath = new LngLat[totalLength];
        int i = 0;
        for (LngLat[] path : paths) {
            for (LngLat lngLat : path) {
                totalPath[i] = lngLat;
                i++;
            }
        }
        return totalPath;
    }

    private LngLat[] findPathToCentralArea(LngLat startPoint) {
        return new LngLat[]{startPoint, startPoint};
    }

    private LngLat[] findPathBetween(LngLat startPoint, LngLat endPoint, NamedRegion maxRegion) {
        return naiveAStar(startPoint, endPoint);
    }

    private LngLat[] naiveAStar(LngLat startPoint, LngLat endPoint) {
        ArrayList<FrontierNode> frontier = new ArrayList<>();
        FrontierNode currentNode = new FrontierNode(0, startPoint, null);
        int counter = 0;
        while (!navigator.isCloseTo(currentNode.currentPosition, endPoint)) {
            List<FrontierNode> nextMoves = filterNoFly(currentNode.getNextMoves());
            frontier.addAll(nextMoves);
            int lowestCostIndex = pickLowestCost(frontier, endPoint);
            currentNode = frontier.get(lowestCostIndex);
            frontier.remove(lowestCostIndex);

            counter += 1;
            if (counter > 3000) {
                break;
            }
        }

        // Construct path from saved graph traversal nodes
        ArrayList<LngLat> pathList = new ArrayList<>();
        while (currentNode.getPreviousNode() != null) {
            pathList.add(currentNode.getCurrentPosition());
            currentNode = currentNode.getPreviousNode();
        }
        Collections.reverse(pathList);
        LngLat[] path = new LngLat[pathList.size()];
        for (int i = 0; i < path.length; i++) {
            path[i] = pathList.get(i);
        }
        return path;
    }

    private List<FrontierNode> filterNoFly(List<FrontierNode> possibleMoves) {
        return possibleMoves.stream().filter(move -> !isNoFlyPosition(move.getCurrentPosition())).toList();
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
            double cost = heuristicCost(node.currentPosition, endPoint) + node.getCostSoFar();
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

    private class FrontierNode {
        private double costSoFar;
        private LngLat currentPosition;
        private FrontierNode previousNode;

        public FrontierNode(double costSoFar, LngLat currentPosition, FrontierNode previousNode) {
            this.costSoFar = costSoFar;
            this.currentPosition = currentPosition;
            this.previousNode = previousNode;
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
            List<FrontierNode> nextMoves = new ArrayList<>(16);
            final double costOfOneMove = SystemConstants.DRONE_MOVE_DISTANCE;
            for (int i = 0; i < 16; i++) {
                LngLat nextPosition = navigator.nextPosition(currentPosition, Math.toRadians(i * 22.5));
                FrontierNode newNode = new FrontierNode(costSoFar + costOfOneMove, nextPosition, this);
                nextMoves.add(newNode);
            }
            return nextMoves;
        }
    }
}