package uk.ac.ed.inf;

import junit.framework.TestCase;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.io.IOException;
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

        Order firstOrder = orders[1];
        RestaurantFinder restaurantFinder = new RestaurantFinder(restaurants);
        PathFinder pathFinder = new PathFinder(noFlyZones, centralArea, CustomConstants.DROP_OFF_POINT);

        logger.log("Computing path...");
        LngLat[] path = pathFinder.computePath(restaurantFinder.getRestaurantForOrder(firstOrder).location());
        logger.log("Path computed!");
        GeoJsonFileWriter fileWriter = new GeoJsonFileWriter("results_test/drone.geojson");
        List<LngLat[]> dronePaths = new ArrayList<>();
        dronePaths.add(path);
        try {
            fileWriter.writePaths(dronePaths);
        }
        catch(IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
