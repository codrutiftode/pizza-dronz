package uk.ac.ed.inf.pathFinder;

import uk.ac.ed.inf.ilp.data.LngLat;

import java.util.HashMap;
import java.util.List;

public class PathCache <CachedItemT> {
    private final HashMap<String, List<CachedItemT>> pathCache;

    public PathCache() {
        pathCache = new HashMap<>();
    }

    public void cache(LngLat targetLocation, List<CachedItemT> path) {
        pathCache.put(targetLocation.toString(), path);
    }

    public boolean has(LngLat targetLocation) {
        return pathCache.containsKey(targetLocation.toString());
    }

    public List<CachedItemT> get(LngLat targetLocation) {
        return pathCache.get(targetLocation.toString());
    }
}
