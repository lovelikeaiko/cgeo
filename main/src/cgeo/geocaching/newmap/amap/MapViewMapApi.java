package cgeo.geocaching.newmap.amap;

import android.os.Bundle;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;

/**
 * Created by paint on 17-5-22.
 */

public class MapViewMapApi extends AMapApi {

    private MapView mMapView;

    public MapViewMapApi(AMap map, MapView mapView) {
        super(map);
        mMapView = mapView;
    }

    @Override
    public void mapOnCreate(Bundle savedInstanceState) {
        mMapView.onCreate(savedInstanceState);
    }

    @Override
    public void mapOnResume() {
        mMapView.onResume();
    }

    @Override
    public void mapOnPause() {
        mMapView.onPause();
    }

    @Override
    public void mapOnStart() {
    }

    @Override
    public void mapOnStop() {
    }

    @Override
    public void mapOnSaveInstanceState(Bundle outState) {
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void mapOnDestroy() {
        mMapView.onDestroy();
    }

    @Override
    public void mapOnLowMemory() {
        mMapView.onLowMemory();
    }
}
