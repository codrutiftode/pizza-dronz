package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import java.util.Arrays;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        CustomLogger logger = CustomLogger.getLogger();

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
    }
}
