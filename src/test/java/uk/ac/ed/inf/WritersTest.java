package uk.ac.ed.inf;

import junit.framework.TestCase;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.pathFinder.FlightMove;
import uk.ac.ed.inf.pathFinder.PathFinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WritersTest extends TestCase {
    public void testDeliveriesWriter() {
        CustomLogger logger = CustomLogger.getLogger();
        String apiUrl = "https://ilp-rest.azurewebsites.net/";
        String targetDate = "2023-11-15";
        OrdersAPIClient apiClient = new OrdersAPIClient(apiUrl);
        Restaurant[] restaurants = apiClient.getRestaurants();
        Order[] orders = apiClient.getOrders(targetDate);

        OrderValidator orderValidator = new OrderValidator();
        List<Order> validOrders = Arrays.stream(orders)
                .map(order -> orderValidator.validateOrder(order, restaurants))
                .filter(order -> order.getOrderValidationCode() == OrderValidationCode.NO_ERROR)
                .toList();

        DeliveriesWriter writer = new DeliveriesWriter("results_test/deliveries.json");
        writer.writeDeliveries(validOrders);
    }

    public void testFlightpathWriter() {
        CustomLogger logger = CustomLogger.getLogger();
        String apiUrl = "https://ilp-rest.azurewebsites.net/";
        String targetDate = "2023-11-15";
        OrdersAPIClient apiClient = new OrdersAPIClient(apiUrl);
        Restaurant[] restaurants = apiClient.getRestaurants();
        Order[] orders = apiClient.getOrders(targetDate);
        NamedRegion centralArea = apiClient.getCentralArea();
        NamedRegion[] noFlyZones = apiClient.getNoFlyZones();

        OrderValidator orderValidator = new OrderValidator();
        List<Order> validOrders = Arrays.stream(orders)
                .map(order -> orderValidator.validateOrder(order, restaurants))
                .filter(order -> order.getOrderValidationCode() == OrderValidationCode.NO_ERROR)
                .toList();

        PathFinder pathFinder = new PathFinder(noFlyZones, centralArea, CustomConstants.DROP_OFF_POINT);
        RestaurantFinder restaurantFinder = new RestaurantFinder(restaurants);
        List<List<FlightMove<LngLat>>> paths = new ArrayList<>();
        LngLat lastDropOff = CustomConstants.DROP_OFF_POINT;
        for (Order order : validOrders) {
            List<FlightMove<LngLat>> path = pathFinder.computePath(lastDropOff, restaurantFinder.getRestaurantForOrder(order).location());
            if (path != null) {
                path.forEach(move -> move.assignToOrder(order.getOrderNo()));
                paths.add(path);
                lastDropOff = path.get(path.size() - 1).getTo();
            }
        }

        FlightpathWriter writer = new FlightpathWriter("results_test/flightpath.json");
        writer.writeFlightpath(paths);
    }
}
