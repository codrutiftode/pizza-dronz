package uk.ac.ed.inf.pathFinder;

import java.util.List;

public interface IFilter<GraphNodeT> {
    public List<GraphNodeT> filterNodes(List<GraphNodeT> possibleMoves, boolean stayInCentral);
}
