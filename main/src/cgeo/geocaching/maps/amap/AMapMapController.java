package cgeo.geocaching.maps.amap;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;

import cgeo.geocaching.maps.interfaces.GeoPointImpl;
import cgeo.geocaching.maps.interfaces.MapControllerImpl;
import com.amap.api.maps.AMap.CancelableCallback;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;

/**
 * Created by paint on 7/20/16.
 */
public class AMapMapController implements MapControllerImpl {

    private final int DEFAULT_ZOOM_LEVEL = 12;

    private AMap aMap;
    private int currentZoomLevel = DEFAULT_ZOOM_LEVEL;

    public AMapMapController(AMap aMap) {
        this.aMap = aMap;
    }

    @Override
    public void setZoom(int mapzoom) {
        changeCamera(CameraUpdateFactory.zoomIn(), null);
    }

    @Override
    public void setCenter(GeoPointImpl geoPoint) {
        // TODO right?
        animateTo(geoPoint);
    }

    @Override
    public void animateTo(GeoPointImpl geoPoint) {
        AMapGeoPoint amapGeoPoint = new AMapGeoPoint(geoPoint.getLatitudeE6()/1e6, geoPoint.getLongitudeE6()/1e6);
        LatLng latLng = new LatLng(amapGeoPoint.getLatitude(), amapGeoPoint.getLatitude());
        changeCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, currentZoomLevel, 0, 0)), null);
    }

    @Override
    public void zoomToSpan(int latSpanE6, int lonSpanE6) {
        //TODO what's the meaning of this? maybe try to copy from Mapsforge

    }

    private void changeCamera(CameraUpdate update, CancelableCallback callback) {
        aMap.animateCamera(update, 1000, callback);
    }

    public int getCurrentZoomLevel(){
        return currentZoomLevel;
    }
}
