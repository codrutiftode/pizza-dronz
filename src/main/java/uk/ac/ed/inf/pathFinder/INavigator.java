package uk.ac.ed.inf.pathFinder;

public interface INavigator<PositionT> {
    boolean isCloseTo(PositionT a, PositionT b);
    PositionT nextPosition(PositionT p, double angle);
}
