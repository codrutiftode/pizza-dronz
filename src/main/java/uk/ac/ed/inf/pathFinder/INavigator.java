package uk.ac.ed.inf.pathFinder;

/**
 * Interface for LngLatHandler that allows A* to use only the minimum required of LngLatHandler.
 * @param <PositionT> type of position for plane used by A*
 */
public interface INavigator<PositionT> {
    boolean isCloseTo(PositionT a, PositionT b);
    PositionT nextPosition(PositionT p, double angle);
}
