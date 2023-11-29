package uk.ac.ed.inf;

import uk.ac.ed.inf.api.APIResult;
import uk.ac.ed.inf.api.OrderValidator;
import uk.ac.ed.inf.api.OrdersAPIClient;
import uk.ac.ed.inf.api.RestaurantFinder;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.pathFinder.FlightMove;
import uk.ac.ed.inf.pathFinder.PathFinder;
import uk.ac.ed.inf.writers.DeliveriesWriter;
import uk.ac.ed.inf.writers.DroneWriter;
import uk.ac.ed.inf.writers.FlightpathWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The main application
 */
public class App 
{
    private static String targetDate;
    private static String apiUrl;

    public static void main(String[] args)
    {
        CustomLogger.getLogger().log("App starting...");
        if (!parseCLIArguments(args)) return;

        // Get data from API
        APIResult apiResult = getDataFromAPI();
        if (!apiResult.getRequestSuccessful()) return;
        Restaurant[] restaurants = apiResult.getRestaurants();
        Order[] orders = apiResult.getOrders();
        NamedRegion[] noFlyZones = apiResult.getNoFlyZones();
        NamedRegion centralArea = apiResult.getCentralArea();

        // Validate orders
        List<Order> ordersWithValidation = validateOrders(orders, restaurants);
        List<Order> validOrders = ordersWithValidation.stream()
                .filter(order -> order.getOrderValidationCode() == OrderValidationCode.NO_ERROR)
                .toList();

        // Find paths
        List<List<FlightMove<LngLat>>> paths = computePaths(validOrders, restaurants, noFlyZones, centralArea);

        // Updates the order status based on whether a path was computed or not
        updateOrdersStatus(ordersWithValidation, validOrders, paths);

        // Output to files
        writeToFiles(ordersWithValidation, paths);
        CustomLogger.getLogger().log("App finished.");
    }

    /**
     * Parses the CLI arguments
     * @param args CLI arguments
     * @return true if CLI arguments were parsed successfully, false otherwise
     */
    private static boolean parseCLIArguments(String[] args) {
        if (args.length != 2) {
            CustomLogger.getLogger().error("Usage: <jar-file> <target-date> <API-URL>.");
            return false;
        }
        targetDate = args[0];
        apiUrl = args[1];
        return true;
    }

    /**
     * Requests and validates data from the API
     * @return an API result that is either unsuccessful, or contains all required data
     */
    private static APIResult getDataFromAPI() {
        // Initialise API connection
        OrdersAPIClient apiClient = new OrdersAPIClient(apiUrl);
        boolean isAlive = apiClient.checkAliveAPI();
        if (!isAlive) {
            CustomLogger.getLogger().error("The API at " + apiUrl + " is not alive. Abort.");
            return new APIResult(false);
        }

        // Check all data was parsed correctly
        if (apiClient.getRestaurants() == null ||
                apiClient.getOrders(targetDate) == null ||
                apiClient.getCentralArea() == null ||
                apiClient.getNoFlyZones() == null) {
            CustomLogger.getLogger().error("There was an error while parsing API responses. Abort.");
            return new APIResult(false);
        }

        // Create normal api result
        return new APIResult(
                apiClient.getOrders(targetDate),
                apiClient.getRestaurants(),
                apiClient.getNoFlyZones(),
                apiClient.getCentralArea()
        );
    }

    /**
     * Adds validation codes for every order
     * @param orders raw list of orders
     * @param restaurants the restaurants
     * @return the same list of orders, but with order validation codes
     */
    private static List<Order> validateOrders(Order[] orders, Restaurant[] restaurants) {
        OrderValidator orderValidator = new OrderValidator();
        return Arrays.stream(orders)
                .map(order -> orderValidator.validateOrder(order, restaurants))
                .toList();
    }

    /**
     * Updates the order status to all valid but not delivered orders,
     * based on whether a path was found for them or not
     * @param allOrders All orders
     * @param validOrders Only the valid orders
     * @param paths The computed paths
     */
    private static void updateOrdersStatus(List<Order> allOrders, List<Order> validOrders, List<List<FlightMove<LngLat>>> paths) {
        int i = 0;
        int j = 0;
        while (i < allOrders.size() && j < validOrders.size()) {
            Order toValidate = allOrders.get(i);
            Order validOrder = validOrders.get(j);
            if (toValidate.getOrderNo().equals(validOrder.getOrderNo())) {
                if (paths.get(j) != null) {
                    toValidate.setOrderStatus(OrderStatus.DELIVERED);
                }
                j++;
            }
            i++;
        }
    }

    /**
     * Tries to compute a path for every valid order
     * @param validOrders a list of valid orders
     * @param restaurants a list of restaurants
     * @param noFlyZones no-fly zones
     * @param centralArea central area
     * @return a list of paths made of flight moves
     */
    private static List<List<FlightMove<LngLat>>> computePaths(List<Order> validOrders,
                                                               Restaurant[] restaurants,
                                                               NamedRegion[] noFlyZones,
                                                               NamedRegion centralArea) {
        PathFinder pathFinder = new PathFinder(noFlyZones, centralArea, CustomConstants.DROP_OFF_POINT);
        RestaurantFinder restaurantFinder = new RestaurantFinder(restaurants);
        List<List<FlightMove<LngLat>>> paths = new ArrayList<>();

        for (Order order : validOrders) {
            LngLat targetLocation = restaurantFinder.getRestaurantForOrder(order).location();
            List<FlightMove<LngLat>> path = pathFinder.computePath(targetLocation);
            if (path != null) {
                path.forEach(move -> move.assignToOrder(order.getOrderNo()));
            }
            paths.add(path);
        }
        return paths;
    }

    /**
     * Write output to JSON and GeoJSON files
     * @param ordersWithValidation orders together with validation codes
     * @param paths list of paths for all valid orders
     */
    private static void writeToFiles(List<Order> ordersWithValidation, List<List<FlightMove<LngLat>>> paths) {
        String deliveriesFile = String.format(CustomConstants.DELIVERIES_FILE_PATH_FORMAT, targetDate);
        String flightpathFile = String.format(CustomConstants.FLIGHTPATH_FILE_PATH_FORMAT, targetDate);
        String droneFile = String.format(CustomConstants.DRONE_FILE_PATH_FORMAT, targetDate);

        new DeliveriesWriter(deliveriesFile).writeDeliveries(ordersWithValidation);
        new FlightpathWriter(flightpathFile).writeFlightpath(paths);
        new DroneWriter(droneFile).writePaths(paths);
    }
}
