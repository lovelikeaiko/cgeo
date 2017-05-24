package cgeo.geocaching.newmap.interfaces;

import android.graphics.Bitmap;
import android.os.Bundle;

import java.util.List;

import cgeo.geocaching.location.Viewport;
import cgeo.geocaching.maps.interfaces.GeoPointImpl;
import cgeo.geocaching.maps.interfaces.MapProjectionImpl;
import cgeo.geocaching.newmap.data.BitmapDescriptor;
import cgeo.geocaching.newmap.data.LatLng;
import cgeo.geocaching.newmap.data.MarkerOptionsInfo;

/**
 * Created by paint on 17-5-22.
 */

public interface MapApiImpl {

    void mapOnCreate(Bundle savedInstanceState);

    void mapOnResume();

    void mapOnPause();

    void mapOnStart();

    void mapOnStop();

    void mapOnSaveInstanceState(Bundle outState);

    void mapOnDestroy();

    void mapOnLowMemory();

    void addMarker(MarkerOptionsInfo info);

    void addMarker(List<MarkerOptionsInfo> infoList);

//    void addMarker(List<BitmapDescriptor> markers);

    void centerMap(GeoPointImpl geoPoint);

    void setZoomLevel(float zoomLevel);

    float getZoomLevel();

    void setMyLocationEnabled(boolean enable);

    void clear();

    Viewport getViewport();

    /**
     * getLatitudeSpan() / 1e6
     * @return 非指数形式
     */
    int getLatitudeSpan();

    int getLongitudeSpan();

    MapProjectionImpl getMapProjection();

    GeoPointImpl getMapViewCenter();
}
