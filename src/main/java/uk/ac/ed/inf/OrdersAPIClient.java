package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

public class OrdersAPIClient extends APIClient {
    private final static String ALIVE_ENDPOINT = "isAlive";
    private final static String RESTAURANTS_ENDPOINT = "restaurants";
    private final static String ORDERS_ENDPOINT = "orders";
    private final static String CENTRAL_AREA_ENDPOINT = "centralArea";
    private final static String NO_FLY_ZONES_ENDPOINT = "noFlyZones";

    public OrdersAPIClient(String apiUrl) {
        super(apiUrl);
    }

    public boolean checkAliveAPI() {
        String data = this.requestGET(this.createEndpoint(ALIVE_ENDPOINT));
        return Boolean.parseBoolean(data); // TODO: what if parse error
    }

    public Restaurant[] getRestaurants() {
        String data = this.requestGET(this.createEndpoint(RESTAURANTS_ENDPOINT));
        return parseRestaurants(data);
    }

    public Order[] getOrders(String targetDate) {
        String data = this.requestGET(this.createEndpoint(ORDERS_ENDPOINT, targetDate));
        return parseOrders(data);
    }

    public NamedRegion getCentralArea() {
        String data = this.requestGET(this.createEndpoint(CENTRAL_AREA_ENDPOINT));
        return parseCentralArea(data);
    }

    public NamedRegion[] getNoFlyZones() {
        String data = this.requestGET(this.createEndpoint(NO_FLY_ZONES_ENDPOINT));
        return parseNoFlyZones(data);
    }

    private Restaurant[] parseRestaurants(String apiResponse) {
        return parseData(apiResponse, Restaurant[].class);
    }

    private NamedRegion parseCentralArea(String apiResponse) {
        return parseData(apiResponse, NamedRegion.class);
    }

    private Order[] parseOrders(String apiResponse) {
        return parseData(apiResponse, Order[].class);
    }

    private NamedRegion[] parseNoFlyZones(String apiResponse) {
        return parseData(apiResponse, NamedRegion[].class);
    }

    private <T> T parseData(String data, Class<T> resultingType) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        try {
            return objectMapper.readValue(data, resultingType);
        } catch (JsonProcessingException e) {
            CustomLogger.getLogger().error("Error while parsing API response of type: " + resultingType.toGenericString() + "\n" + e.getMessage());
            return null;
        }
    }
}
