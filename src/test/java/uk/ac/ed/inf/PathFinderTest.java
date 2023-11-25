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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathFinderTest extends TestCase {
    public void test1() {
        LngLat dropOff = new LngLat(5, 5);
        NamedRegion[] noFlyZones = new NamedRegion[]{
                new NamedRegion("no-fly-1", new LngLat[]{
                        new LngLat(0, 0),
                        new LngLat(0, 3),
                        new LngLat(3, 3)
                })
        };
        NamedRegion centralArea = new NamedRegion("central-area",
                new LngLat[]{
                        new LngLat(0, 0),
                        new LngLat(0, 10),
                        new LngLat(10, 10),
                        new LngLat(10, 0)
                });
        LngLat targetLocation = new LngLat(7, 7);
        PathFinder pathFinder = new PathFinder(noFlyZones, centralArea,dropOff);
        List<FlightMove<LngLat>> path = pathFinder.computePath(dropOff, targetLocation);
        System.out.println(Arrays.toString(path.toArray()));
    }

    public void testOnePath() {
        CustomLogger logger = CustomLogger.getLogger();
        String apiUrl = "https://ilp-rest.azurewebsites.net/";
        String targetDate = "2023-11-15";
        OrdersAPIClient apiClient = new OrdersAPIClient(apiUrl);
        Restaurant[] restaurants = apiClient.getRestaurants();
        Order[] orders = apiClient.getOrders(targetDate);
        NamedRegion centralArea = new NamedRegion("central", new LngLat[]{
//                new LngLat(-3.1925, 55.9436),
//                new LngLat(-3.1925, 55.9426),
                new LngLat(-3.1937, 55.9437),
                new LngLat(	-3.1938, 55.9426),
                new LngLat(-3.1860, 55.9426),
                new LngLat(-3.1860, 55.9451),
                new LngLat(	-3.1876, 55.9453),
                new LngLat(-3.1876, 55.9439)
        });
        NamedRegion[] noFlyZones = apiClient.getNoFlyZones();

        OrderValidator orderValidator = new OrderValidator();
        List<Order> validOrders = Arrays.stream(orders)
                .map(order -> orderValidator.validateOrder(order, restaurants))
                .filter(order -> order.getOrderValidationCode() == OrderValidationCode.NO_ERROR)
                .toList();

        DroneWriter fileWriter = new DroneWriter("results_test/drone.geojson");
        RestaurantFinder restaurantFinder = new RestaurantFinder(restaurants);
        LngLat lastDropOff = CustomConstants.DROP_OFF_POINT;
        logger.log("Computing path...");
        PathFinder pathFinder = new PathFinder(noFlyZones, centralArea, CustomConstants.DROP_OFF_POINT);
        Order order = validOrders.get(0);
        List<FlightMove<LngLat>> path = pathFinder.computePath(lastDropOff, restaurantFinder.getRestaurantForOrder(order).location());
        logger.log("Path computed.");

        List<List<FlightMove<LngLat>>> list = new ArrayList<>();
        list.add(path);
        fileWriter.writePaths(list);
    }
}
