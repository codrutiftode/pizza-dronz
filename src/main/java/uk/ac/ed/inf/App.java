package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonProcessingException;
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
        if (args.length != 2) {
            logger.error("Usage: <jar-file> <target-date> <API-URL>.");
            return;
        }

        String date = args[0];
        String apiUrl = args[1];

        OrdersAPIClient apiClient = new OrdersAPIClient(apiUrl);
        boolean isAlive = apiClient.checkAliveAPI();

        if (isAlive) {
            try {
                Restaurant[] restaurants = apiClient.getRestaurants();
                logger.log(Arrays.toString(restaurants));
            }
            catch(JsonProcessingException e) {
                logger.error("There was an error when parsing JSON data from the API.");
                return;
            }

        }
    }
}
