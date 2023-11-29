package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;

/**
 * Constants for my implementation that were not added to SystemConstants
 */
public class CustomConstants {
    public static final LngLat DROP_OFF_POINT =  new LngLat(-3.186874, 55.944494);
    public static final double ANGLE_WHEN_HOVER = 999;
    public static final String DRONE_FILE_PATH_FORMAT = "resultfiles/drone-%s.geojson";
    public static final String FLIGHTPATH_FILE_PATH_FORMAT = "resultfiles/flightpath-%s.json";
    public static final String DELIVERIES_FILE_PATH_FORMAT = "resultfiles/deliveries-%s.json";
    public static final int DIRECTIONS_CIRCLE_DIVISIONS = 16;

    /**
     * The maximum number of nodes that can be expanded from the frontier
     * before A* gives up.
     */
    public static final int DEFAULT_FRONTIER_EXPANSION_LIMIT = 40000;

    /**
     * API Endpoints
     */
    public final static String ALIVE_ENDPOINT = "isAlive";
    public final static String RESTAURANTS_ENDPOINT = "restaurants";
    public final static String ORDERS_ENDPOINT = "orders";
    public final static String CENTRAL_AREA_ENDPOINT = "centralArea";
    public final static String NO_FLY_ZONES_ENDPOINT = "noFlyZones";
}
