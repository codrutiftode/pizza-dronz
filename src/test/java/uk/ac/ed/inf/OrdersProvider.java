package uk.ac.ed.inf;

import uk.ac.ed.inf.api.OrdersAPIClient;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class OrdersProvider {

    private final Restaurant[] restaurants;
    private final Order[] orders;
    private final NamedRegion[] noFlyZones;
    private final NamedRegion centralArea;

    public OrdersProvider() {
        DayOfWeek[] daysOpen1 = new DayOfWeek[]{
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
        };
        DayOfWeek[] daysOpen2 = new DayOfWeek[]{
                LocalDate.now().getDayOfWeek().plus(1)
        };
        this.restaurants = new Restaurant[]{
                new Restaurant("Civs", new LngLat(-3.1890,55.9470), daysOpen1, new Pizza[]{
                        new Pizza("A", 100),
                        new Pizza("B", 200)
                }),
                new Restaurant("Sodeberg Pavillion", new LngLat( -3.1940174102783203, 55.94390696616939), daysOpen2, new Pizza[]{
                        new Pizza("C", 300),
                        new Pizza("D", 400),
                        new Pizza("E", 500)
                }),
        };

        this.orders = new Order[] {
                new Order("0", LocalDate.now(), OrderStatus.UNDEFINED, OrderValidationCode.UNDEFINED, 300,
                        new Pizza[]{new Pizza("A", 100), new Pizza("B", 200)},
                        null),
                new Order("1", LocalDate.now(), OrderStatus.UNDEFINED, OrderValidationCode.UNDEFINED, 300,
                        new Pizza[]{new Pizza("C", 300), new Pizza("D", 400), new Pizza("E", 500)},
                        null)
        };

        OrdersAPIClient client = new OrdersAPIClient("https://ilp-rest.azurewebsites.net");
        this.noFlyZones = client.getNoFlyZones();
        this.centralArea = client.getCentralArea();
    }

    public Restaurant[] getRestaurants() {
        return restaurants;
    }

    public Order[] getOrders() {
        return orders;
    }

    public NamedRegion[] getNoFlyZones() {
        return noFlyZones;
    }

    public NamedRegion getCentralArea() {
        return centralArea;
    }
}
