package cgeo.geocaching.newmap.interfaces;

import cgeo.geocaching.maps.interfaces.GeoPointImpl;
import cgeo.geocaching.newmap.data.LatLng;

/**
 * Created by paint on 17-5-23.
 */

public interface MapApiListener {

    void onMapMoved(GeoPointImpl center);
}
