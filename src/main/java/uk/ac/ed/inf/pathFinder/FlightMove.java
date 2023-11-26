package uk.ac.ed.inf.pathFinder;

public class FlightMove<PositionT> {
    private final PositionT from;
    private final PositionT to;
    private final double angle;
    private final long elapsedTicks;
    private String orderNo;

    public FlightMove(PositionT from, PositionT to, double angle, long elapsedTicks) {
        this.from = from;
        this.to = to;
        this.angle = angle;
        this.elapsedTicks = elapsedTicks;
    }

    public void assignToOrder(String orderNo) {
        this.orderNo = orderNo;
    }

    public PositionT getFrom() {
        return from;
    }

    public PositionT getTo() {
        return to;
    }

    public double getAngle() {
        return angle;
    }

    public long getElapsedTicks() {
        return elapsedTicks;
    }

    public String getOrderNo() {
        return orderNo;
    }
}
