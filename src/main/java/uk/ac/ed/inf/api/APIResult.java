package uk.ac.ed.inf.api;

import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

public class APIResult {
    private final Order[] orders;
    private final Restaurant[] restaurants;
    private final NamedRegion[] noFlyZones;
    private final NamedRegion centralArea;
    private final boolean requestSuccessful;
    public APIResult(Order[] orders,
                     Restaurant[] restaurants,
                     NamedRegion[] noFlyZones,
                     NamedRegion centralArea) {
        this.orders = orders;
        this.restaurants = restaurants;
        this.noFlyZones = noFlyZones;
        this.centralArea = centralArea;
        this.requestSuccessful = true;
    }

    public APIResult(boolean requestSuccessful) {
        this.requestSuccessful = requestSuccessful;
        this.orders = null;
        this.restaurants = null;
        this.noFlyZones = null;
        this.centralArea = null;
    }

    public Order[] getOrders() {
        return orders;
    }

    public Restaurant[] getRestaurants() {
        return restaurants;
    }

    public NamedRegion[] getNoFlyZones() {
        return noFlyZones;
    }

    public NamedRegion getCentralArea() {
        return centralArea;
    }

    public boolean getRequestSuccessful() {
        return requestSuccessful;
    }
}
