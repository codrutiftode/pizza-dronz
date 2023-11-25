package uk.ac.ed.inf.writers;

import com.google.gson.*;
import uk.ac.ed.inf.CustomLogger;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.writers.CustomFileWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class DeliveriesWriter extends CustomFileWriter {
    public DeliveriesWriter(String filepath) {
        super(filepath);
    }

    public void writeDeliveries(List<Order> orders) {
        try {
            Gson gson = new GsonBuilder().registerTypeAdapter(Order.class, new DeliverySerialiser()).create();
            this.write(gson.toJson(orders));
        }
        catch(IOException e) {
            CustomLogger.getLogger().error("Failed to write deliveries file:\n" + e.getMessage());
        }
    }

    private class DeliverySerialiser implements JsonSerializer<Order> {
        @Override
        public JsonElement serialize(Order order, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject json = new JsonObject();
            json.add("orderNo", new JsonPrimitive(order.getOrderNo()));
            json.add("orderStatus", new JsonPrimitive(order.getOrderStatus().toString()));
            json.add("orderValidationCode", new JsonPrimitive(order.getOrderValidationCode().toString()));
            json.add("costInPence", new JsonPrimitive(order.getPriceTotalInPence() + 100));
            return json;
        }
    }
}
