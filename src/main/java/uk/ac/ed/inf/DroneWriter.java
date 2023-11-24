package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.geojson.*;
import uk.ac.ed.inf.ilp.data.LngLat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DroneWriter extends CustomFileWriter {
    public DroneWriter(String filepath) {
        super(filepath);
    }

    public void writePaths(List<List<FlightMove>> paths) {
        List<LngLat> dronePath = compileDronePath(paths);
        JsonElement geoJson = buildGeoJson(dronePath);
        String output = new GsonBuilder().setPrettyPrinting().create().toJson(geoJson);
        try {
            this.write(output);
        }
        catch(IOException e) {
            CustomLogger.getLogger().error("Failed to write drone file:\n" + e.getMessage());
        }
    }

    private JsonElement buildGeoJson(List<LngLat> dronePath) {
        JsonObject featureCollection = new JsonObject();
        JsonArray features = new JsonArray();
        featureCollection.add("type", new JsonPrimitive("FeatureCollection"));
        featureCollection.add("features", features);

        JsonObject feature = new JsonObject();
        features.add(feature);
        JsonObject lineString = new JsonObject();
        feature.add("type", new JsonPrimitive("Feature"));
        feature.add("geometry", lineString);
        feature.add("properties", new JsonObject());

        JsonArray allCoordinates = new JsonArray();
        lineString.add("type", new JsonPrimitive("LineString"));
        lineString.add("coordinates", allCoordinates);

        for (LngLat move : dronePath) {
            JsonArray coordinates = new JsonArray();
            coordinates.add(move.lng());
            coordinates.add(move.lat());
            allCoordinates.add(coordinates);
        }
        return featureCollection;
    }

    private List<LngLat> compileDronePath(List<List<FlightMove>> paths) {
        List<LngLat> dronePath = new ArrayList<>();
        for (List<FlightMove> path : paths) {
            dronePath.addAll(path.stream().map(FlightMove::getFrom).toList());
            FlightMove lastMove = path.get(path.size() - 1);
            dronePath.add(lastMove.getTo());
        }
        return dronePath;
    }
}
