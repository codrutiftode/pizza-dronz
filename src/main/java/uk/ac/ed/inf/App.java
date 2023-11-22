package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        CustomLogger logger = CustomLogger.getLogger();
        logger.log("App starting...");

        // Read CLI arguments
        if (args.length != 2) {
            logger.error("Usage: <jar-file> <target-date> <API-URL>.");
            return;
        }
        String targetDate = args[0];
        String apiUrl = args[1];

        // Initialise API connection
        OrdersAPIClient apiClient = new OrdersAPIClient(apiUrl);
        boolean isAlive = apiClient.checkAliveAPI();
        if (!isAlive) {
            logger.error("The API at " + apiUrl + " is not alive. Abort.");
            return;
        }

        // Get data from API
        Restaurant[] restaurants = apiClient.getRestaurants();
        Order[] orders = apiClient.getOrders(targetDate);
        NamedRegion[] noFlyZones = apiClient.getNoFlyZones();
        NamedRegion centralArea = apiClient.getCentralArea();
        if (restaurants == null || orders == null || noFlyZones == null || centralArea == null) {
            logger.error("There was an error while parsing API responses. Abort.");
            return;
        }

        // Validate orders
        OrderValidator orderValidator = new OrderValidator();
        List<Order> validOrders = Arrays.stream(orders)
                .map(order -> orderValidator.validateOrder(order, restaurants))
                .filter(order -> order.getOrderValidationCode() == OrderValidationCode.NO_ERROR)
                .toList();

        // Find paths
        PathFinder pathFinder = new PathFinder(noFlyZones, centralArea, CustomConstants.DROP_OFF_POINT);
        RestaurantFinder restaurantFinder = new RestaurantFinder(restaurants);
        List<LngLat[]> paths = validOrders.stream()
                .map(order -> pathFinder.computePath(CustomConstants.DROP_OFF_POINT, restaurantFinder.getRestaurantForOrder(order).location())) // TODO: change drop off point to last drop off point
                .filter(Objects::nonNull)
                .toList();

        JsonFileWriter deliveriesWriter = new JsonFileWriter("resultfiles/deliveries.json");
        JsonFileWriter flightpathWriter = new JsonFileWriter("resultfiles/flightpath.json");
        GeoJsonFileWriter droneWriter = new GeoJsonFileWriter("resultfiles/drone.json");
        // TODO: finish this
    }
}
