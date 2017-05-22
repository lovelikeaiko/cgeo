package cgeo.geocaching.newmap.interfaces;

import android.os.Bundle;

import cgeo.geocaching.maps.interfaces.GeoPointImpl;
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

    void centerMap(GeoPointImpl geoPoint);

    void setZoomLevel(float zoomLevel);

    void setMyLocationEnabled(boolean enable);

    void clear();
}
