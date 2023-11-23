package uk.ac.ed.inf;

import junit.framework.TestCase;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

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
}
