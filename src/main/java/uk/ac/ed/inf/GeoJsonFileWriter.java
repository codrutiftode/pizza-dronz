package uk.ac.ed.inf;

import org.geojson.*;
import uk.ac.ed.inf.ilp.data.LngLat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeoJsonFileWriter extends JsonFileWriter {
    public GeoJsonFileWriter(String filepath) {
        super(filepath);
    }

    public void writePaths(List<LngLat[]> paths) throws IOException {
        List<LngLat> dronePath = compileDronePath(paths);
        FeatureCollection fc = buildGeoJson(dronePath);
        this.write(fc);
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

    private List<LngLat> compileDronePath(List<LngLat[]> paths) {
        List<LngLat> dronePath = new ArrayList<>();
        for (LngLat[] path : paths) {
            dronePath.addAll(Arrays.stream(path).toList());
        }
        return dronePath;
    }
}
