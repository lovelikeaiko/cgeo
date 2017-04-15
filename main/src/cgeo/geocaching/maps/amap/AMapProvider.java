package cgeo.geocaching.maps.amap;

import android.app.Activity;
import android.support.annotation.NonNull;


import cgeo.geocaching.R;
import cgeo.geocaching.maps.AbstractMapProvider;
import cgeo.geocaching.maps.AbstractMapSource;
import cgeo.geocaching.maps.interfaces.MapItemFactory;
import cgeo.geocaching.maps.interfaces.MapProvider;
import cgeo.geocaching.maps.interfaces.MapSource;

/**
 * Created by paint on 7/30/16.
 */
public class AMapProvider extends AbstractMapProvider {

    public static final String AMAP_BASE_ID = "AMAP_BASE";
    public static final String AMAP_SATELLITE_ID = "AMAP_SATELLITE";


    public AMapProvider() {
        registerMapSource(new AMapBaseMapSource(this, "AMap"));
        registerMapSource(new AMapBaseMapSource(this, "AMap: Satellite"));
    }

    private static class Holder {
        private static final AMapProvider INSTANCE = new AMapProvider();
    }

    public static AMapProvider getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean isSameActivity(MapSource source1, MapSource source2) {
        //TODO ?
        return true;
    }

    @Override
    public Class<? extends Activity> getMapClass() {
        return null;
    }

    @Override
    public int getMapViewId() {
        return R.id.map;
    }

    @Override
    public int getMapLayoutId() {
        return -1;
    }

    @Override
    public MapItemFactory getMapItemFactory() {
        return null;
    }

    private abstract static class AbstractAMapMapSource extends AbstractMapSource {
        public AbstractAMapMapSource(String id, @NonNull MapProvider mapProvider, String name) {
            super(id, mapProvider, name);
        }
    }

    private class AMapBaseMapSource extends AbstractMapSource {

        public AMapBaseMapSource(MapProvider mapProvider, String name) {
            super(AMAP_BASE_ID, mapProvider, name);
        }
    }

    private class AMapSatelliteMapSource extends AbstractMapSource {

        public AMapSatelliteMapSource(MapProvider mapProvider, String name) {
            super(AMAP_SATELLITE_ID, mapProvider, name);
        }
    }

}
