package uk.ac.ed.inf;

import com.google.gson.GsonBuilder;
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
        FeatureCollection fc = buildGeoJson(dronePath);
        String output = new GsonBuilder().setPrettyPrinting().create().toJson(fc);
        try {
            this.write(output);
        }
        catch(IOException e) {
            CustomLogger.getLogger().error("Failed to write drone file:\n" + e.getMessage());
        }
    }

    private FeatureCollection buildGeoJson(List<LngLat> dronePath) {
        FeatureCollection fc = new FeatureCollection();
        Feature feature = new Feature();
        LineString lineString = new LineString();

        for (LngLat move : dronePath) {
            LngLatAlt coordinates = new LngLatAlt();
            coordinates.setLatitude(move.lat());
            coordinates.setLongitude(move.lng());
            lineString.add(coordinates);
        }
        feature.setGeometry(lineString);
        fc.add(feature);
        return fc;
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
