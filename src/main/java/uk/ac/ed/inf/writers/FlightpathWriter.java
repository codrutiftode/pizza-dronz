package uk.ac.ed.inf.writers;

import com.google.gson.*;
import uk.ac.ed.inf.CustomLogger;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.pathFinder.FlightMove;
import uk.ac.ed.inf.writers.CustomFileWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

public class FlightpathWriter extends CustomFileWriter {
    public FlightpathWriter(String flightpath) {
        super(flightpath);
    }

    public void writeFlightpath(List<List<FlightMove<LngLat>>> paths) {
        Gson gson = new GsonBuilder().registerTypeAdapter(FlightMove.class, new FlightpathSerialiser()).create();
        String output = gson.toJson(flatten(paths));
        try {
            this.write(output);
        }
        catch (IOException e) {
            CustomLogger.getLogger().error("Could not write flightpath file.\n" + e.getMessage());
        }
    }

    private <T> List<T> flatten(List<List<T>> listOfLists) {
        return listOfLists.stream().flatMap(List::stream).collect(Collectors.toList());
    }

    private static class FlightpathSerialiser implements JsonSerializer<FlightMove<LngLat>> {
        @Override
        public JsonElement serialize(FlightMove<LngLat> flightMove, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject object = new JsonObject();
            double fromLongitude = flightMove.getFrom().lng();
            double fromLatitude = flightMove.getFrom().lat();
            double toLongitude = flightMove.getTo().lng();
            double toLatitude = flightMove.getTo().lat();
            object.add("orderNo", new JsonPrimitive(flightMove.getOrderNo()));
            object.add("fromLongitude", new JsonPrimitive(fromLongitude));
            object.add("fromLatitude", new JsonPrimitive(fromLatitude));
            object.add("angle", new JsonPrimitive(flightMove.getAngle()));
            object.add("toLongitude", new JsonPrimitive(toLongitude));
            object.add("toLatitude", new JsonPrimitive(toLatitude));
            object.add("ticksSinceStartOfCalculation", new JsonPrimitive(flightMove.getElapsedTicks()));
            return object;
        }
    }
}
