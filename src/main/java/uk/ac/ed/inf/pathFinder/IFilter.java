package uk.ac.ed.inf.pathFinder;

import java.util.List;

/**
 * Filters a list of graph nodes
 * @param <GraphNodeT> the type of nodes used in the list
 */
public interface IFilter<GraphNodeT> {
    List<GraphNodeT> filterNodes(List<GraphNodeT> possibleMoves, boolean stayInCentral);
}
