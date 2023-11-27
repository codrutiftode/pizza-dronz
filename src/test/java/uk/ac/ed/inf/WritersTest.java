package uk.ac.ed.inf;

import com.google.gson.stream.JsonReader;
import junit.framework.TestCase;
import uk.ac.ed.inf.api.OrderValidator;
import uk.ac.ed.inf.api.OrdersAPIClient;
import uk.ac.ed.inf.api.RestaurantFinder;
import uk.ac.ed.inf.coordinates.LngLatHandler;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.pathFinder.FlightMove;
import uk.ac.ed.inf.pathFinder.PathFinder;
import uk.ac.ed.inf.writers.DeliveriesWriter;
import uk.ac.ed.inf.writers.FlightpathWriter;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WritersTest extends TestCase {
    public void testDeliveriesWriter() {
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

    public void testFlightpathWriter_OneOrder() throws IOException {
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
        Order order = validOrders.get(0);
        List<FlightMove<LngLat>> path = pathFinder.computePath(lastDropOff, restaurantFinder.getRestaurantForOrder(order).location());
        if (path != null) {
            path.forEach(move -> move.assignToOrder(order.getOrderNo()));
            paths.add(path);
        }

        FlightpathWriter writer = new FlightpathWriter("results_test/flightpath.json");
        writer.writeFlightpath(paths);

        // Test file validity
        this.testFlightpathFile(restaurantFinder.getRestaurantForOrder(order));
    }

    private void testFlightpathFile(Restaurant restaurant) throws IOException {
        JsonReader reader = new JsonReader(new FileReader("results_test/flightpath.json"));
        LngLatHandler handler = new LngLatHandler();
        reader.beginArray();
        boolean validFile = true;
        int hoverCounter = 0;
        while (reader.hasNext()) {
            reader.beginObject();
            reader.nextName();
            reader.nextString();
            reader.nextName();
            double fromLng = reader.nextDouble();
            reader.nextName();
            double fromLat = reader.nextDouble();
            reader.nextName();
            double angle = reader.nextDouble();
            reader.nextName();
            double toLng = reader.nextDouble();
            reader.nextName();
            double toLat = reader.nextDouble();

            double distance = handler.distanceTo(new LngLat(fromLng, fromLat), new LngLat(toLng, toLat));
            boolean validMove = handler.doublesEqual(distance, SystemConstants.DRONE_MOVE_DISTANCE) || distance == 0;
            boolean validHoverMove = distance != 0 || (angle == 999 && handler.doublesEqual(fromLng, toLng) && handler.doublesEqual(fromLat, toLat));
            if (distance == 0) hoverCounter += 1;
            LngLat locationToCompare = hoverCounter == 1 ? restaurant.location() : hoverCounter == 2 ? CustomConstants.DROP_OFF_POINT : null;
            if (locationToCompare != null && distance == 0) {
                boolean closeTo = handler.isCloseTo(new LngLat(toLng, toLat), locationToCompare);
                validFile = validFile && closeTo;
            }

            validFile = validFile && validMove && validHoverMove;
            reader.endObject();
        }
        assertTrue(validFile);
        reader.endArray();
        reader.close();
    }
}
