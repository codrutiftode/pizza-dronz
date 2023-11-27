package uk.ac.ed.inf;

import junit.framework.TestCase;
import uk.ac.ed.inf.api.OrderValidator;
import uk.ac.ed.inf.api.OrdersAPIClient;
import uk.ac.ed.inf.api.RestaurantFinder;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.pathFinder.FlightMove;
import uk.ac.ed.inf.pathFinder.PathFinder;
import uk.ac.ed.inf.writers.DroneWriter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeojsonTest extends TestCase {
    public void test1() {
        CustomLogger logger = CustomLogger.getLogger();
        OrdersProvider provider = new OrdersProvider();
        Restaurant[] restaurants = provider.getRestaurants();
        Order[] orders = provider.getOrders();
        NamedRegion centralArea = provider.getCentralArea();
        NamedRegion[] noFlyZones = provider.getNoFlyZones();

//        OrderValidator orderValidator = new OrderValidator();
//        List<Order> validOrders = Arrays.stream(orders)
//                .map(order -> orderValidator.validateOrder(order, restaurants))
//                .filter(order -> order.getOrderValidationCode() == OrderValidationCode.NO_ERROR)
//                .toList();

        Order firstOrder = orders[0];
        RestaurantFinder restaurantFinder = new RestaurantFinder(restaurants);
        PathFinder pathFinder = new PathFinder(noFlyZones, centralArea, CustomConstants.DROP_OFF_POINT);

        logger.log("Computing path...");
        List<FlightMove<LngLat>> path = pathFinder.computePath(CustomConstants.DROP_OFF_POINT, restaurantFinder.getRestaurantForOrder(firstOrder).location());
        logger.log("Path computed!");
        DroneWriter fileWriter = new DroneWriter("results_test/drone.geojson");
        List<List<FlightMove<LngLat>>> dronePaths = new ArrayList<>();
        dronePaths.add(path);
        fileWriter.writePaths(dronePaths);
    }

    public void test2() {
        CustomLogger logger = CustomLogger.getLogger();
        String apiUrl = "https://ilp-rest.azurewebsites.net/";
        String targetDate = "2023-11-20";
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
        logger.log("No. valid orders: " + validOrders.size());

        DroneWriter fileWriter = new DroneWriter("results_test/drone.geojson");
        List<List<FlightMove<LngLat>>> dronePaths = new ArrayList<>();
        RestaurantFinder restaurantFinder = new RestaurantFinder(restaurants);
        LngLat lastDropOff = CustomConstants.DROP_OFF_POINT;

        for (int i = 0; i < validOrders.size(); i++) {
            Order order = validOrders.get(i);
            PathFinder pathFinder = new PathFinder(noFlyZones, centralArea, CustomConstants.DROP_OFF_POINT);
            logger.log("Computing path " + i + "...");
            List<FlightMove<LngLat>> path = pathFinder.computePath(lastDropOff, restaurantFinder.getRestaurantForOrder(order).location());
            logger.log("Path " + i + " computed!");
            dronePaths.add(path);
        }
        fileWriter.writePaths(dronePaths);
    }

    public void testCountValidOrders() {
        CustomLogger logger = CustomLogger.getLogger();
        String apiUrl = "https://ilp-rest.azurewebsites.net/";
        LocalDate date = LocalDate.now();
        OrdersAPIClient apiClient = new OrdersAPIClient(apiUrl);
        Restaurant[] restaurants = apiClient.getRestaurants();

        int i = 0;
        while (i < 100) {
            date = date.plus(1, ChronoUnit.DAYS);
            Order[] orders = apiClient.getOrders(date.toString());
            OrderValidator orderValidator = new OrderValidator();
            List<Order> validOrders = Arrays.stream(orders)
                    .map(order -> orderValidator.validateOrder(order, restaurants))
                    .filter(order -> order.getOrderValidationCode() == OrderValidationCode.NO_ERROR)
                    .toList();

            logger.log("Date: " + date + " <-> valid orders: " + validOrders.size());
            i++;
        }
    }

    public void testCountValidOrders2() {
        CustomLogger logger = CustomLogger.getLogger();
        String apiUrl = "https://ilp-rest.azurewebsites.net/";
        String date = "2023-11-15";
        OrdersAPIClient apiClient = new OrdersAPIClient(apiUrl);
        Restaurant[] restaurants = apiClient.getRestaurants();

        Order[] orders = apiClient.getOrders(date);
        OrderValidator orderValidator = new OrderValidator();
        List<Order> validOrders = Arrays.stream(orders)
                .map(order -> orderValidator.validateOrder(order, restaurants))
                .filter(order -> order.getOrderValidationCode() == OrderValidationCode.NO_ERROR)
                .toList();

        logger.log(validOrders.toString());

        logger.log("Date: " + date + " <-> valid orders: " + validOrders.size());

    }
}
