package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public Restaurant[] getRestaurants() throws JsonProcessingException {
        String data = this.requestGET(this.createEndpoint(RESTAURANTS_ENDPOINT));
        return parseRestaurants(data);
    }

    public Order[] getOrders() throws JsonProcessingException {
        String data = this.requestGET(this.createEndpoint(ORDERS_ENDPOINT));
        return parseOrders(data);
    }

    public NamedRegion getCentralArea() throws JsonProcessingException {
        String data = this.requestGET(this.createEndpoint(CENTRAL_AREA_ENDPOINT));
        return parseCentralArea(data);
    }

    public NamedRegion[] getNoFlyZones() throws JsonProcessingException {
        String data = this.requestGET(this.createEndpoint(NO_FLY_ZONES_ENDPOINT));
        return parseNoFlyZones(data);
    }

    private Restaurant[] parseRestaurants(String apiResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Restaurant[] a = new Restaurant[0];
        return objectMapper.readValue(apiResponse, a.getClass());
    }

    private NamedRegion parseCentralArea(String apiResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(apiResponse, NamedRegion.class);
    }

    private Order[] parseOrders(String apiResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Order[] a = new Order[0];
        return objectMapper.readValue(apiResponse, a.getClass());
    }

    private NamedRegion[] parseNoFlyZones(String apiResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        NamedRegion[] a = new NamedRegion[0];
        return objectMapper.readValue(apiResponse, a.getClass());
    }
}
