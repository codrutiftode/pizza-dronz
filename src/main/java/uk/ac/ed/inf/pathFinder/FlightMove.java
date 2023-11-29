package uk.ac.ed.inf.pathFinder;

public class FlightMove<PositionT> {
    private final PositionT from;
    private final PositionT to;
    private final double angle;
    private String orderNo;

    public FlightMove(PositionT from, PositionT to, double angle) {
        this.from = from;
        this.to = to;
        this.angle = angle;
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

    public String getOrderNo() {
        return orderNo;
    }
}
