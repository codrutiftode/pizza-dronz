package uk.ac.ed.inf.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import uk.ac.ed.inf.CustomLogger;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.ilp.gsonUtils.LocalDateDeserializer;

import java.time.LocalDate;

import static uk.ac.ed.inf.CustomConstants.*;

/**
 * Connects to server and parses the order information
 */
public class OrdersAPIClient extends APIClient {

    public OrdersAPIClient(String apiUrl) {
        super(apiUrl);
    }

    public boolean checkAliveAPI() {
        String data = this.requestGET(this.createEndpoint(ALIVE_ENDPOINT));
        return Boolean.TRUE.equals(parseData(data, Boolean.class));
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

    /**
     * Tries to parse a given string into the given type
     * @param data the string
     * @param resultingType the type to interpret the string as
     * @return the equivalent Java Object of type resultingType if successful
     * @param <T> Allows for resultingType to be any type
     */
    private <T> T parseData(String data, Class<T> resultingType) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        Gson gson = builder.create();
        try {
            return gson.fromJson(data, resultingType);
        } catch (JsonSyntaxException e) {
            CustomLogger.getLogger().error("Error while parsing API response of type: " + resultingType.toGenericString() + "\n" + e.getMessage());
            return null;
        }
    }
}
