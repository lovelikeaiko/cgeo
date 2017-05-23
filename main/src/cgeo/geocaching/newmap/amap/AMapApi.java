package cgeo.geocaching.newmap.amap;

import android.util.SparseArray;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;

import java.util.ArrayList;

import cgeo.geocaching.maps.amap.AMapGeoPoint;
import cgeo.geocaching.maps.interfaces.GeoPointImpl;
import cgeo.geocaching.newmap.BaseMapApi;
import cgeo.geocaching.newmap.data.BitmapDescriptor;
import cgeo.geocaching.newmap.data.MarkerOptionsInfo;
import cgeo.geocaching.newmap.interfaces.BuildMarkerOpionInfoCallback;
import cgeo.geocaching.newmap.interfaces.MapApiImpl;

/**
 * Created by paint on 17-5-22.
 */

public abstract class AMapApi extends BaseMapApi {


    private final float DEFAULT_ZOOM_LEVEL = 12;

    private float currentZoomLevel = DEFAULT_ZOOM_LEVEL;

    private SparseArray<MarkerOptions> mMarkerOptionsCache;

    private AMap mAMap = null;

    private MarkerOptions mTempMarkerOptions;

    public AMapApi(AMap map) {
        mAMap = map;
        mMarkerOptionsCache = new SparseArray<>();
    }

    public static MapApiImpl createMapApi(AMap map, MapView mapView){
        return new MapViewMapApi(map, mapView);
    }

    public static MapApiImpl createMapApi(AMap map){
        return new SupportMapFragmentMapApi(map);
    }

    private BuildMarkerOpionInfoCallback mBuildCallback = new BuildMarkerOpionInfoCallback() {



        @Override
        public void onBuildStart(MarkerOptionsInfo info) {
            mTempMarkerOptions = new MarkerOptions();
        }

        @Override
        public void onBuildEnd(MarkerOptionsInfo info) {
//            mMarkerOptionsCache.put(info.);
        }

        @Override
        public void icons(ArrayList<BitmapDescriptor> icons) {
            mTempMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(icons.get(0).getBitmap()));
        }

        @Override
        public void period(int period) {

        }

        @Override
        public void perspective(boolean perspective) {

        }

        @Override
        public void position(cgeo.geocaching.newmap.data.LatLng position) {

        }

        @Override
        public void setFlat(boolean flat) {

        }

        @Override
        public void anchor(float u, float v) {

        }

        @Override
        public void setInfoWindowOffset(int offsetX, int offsetY) {

        }

        @Override
        public void title(String title) {

        }

        @Override
        public void snippet(String snippet) {

        }

        @Override
        public void draggable(boolean draggable) {

        }

        @Override
        public void visible(boolean visible) {

        }

        @Override
        public void setGps(boolean gps) {

        }

        @Override
        public void zIndex(float zIndex) {

        }

        @Override
        public void alpha(float alpha) {

        }

        @Override
        public void displayLevel(int displayLevel) {

        }

        @Override
        public void rotateAngle(float rotateAngle) {

        }

        @Override
        public void infoWindowEnable(boolean infoWindowEnable) {

        }
    };

    @Override
    public void addMarkerInMainThread(MarkerOptionsInfo info) {
        MarkerOptions markerOptions = getMarkerOptions(info.getIconInfo(), info.getOptionsSetState());
        if (markerOptions==null){
            info.buildMarkerOpionInfo(mBuildCallback);
        }
        mAMap.addMarker(mTempMarkerOptions);
    }

    @Override
    public void centerMap(GeoPointImpl geoPoint) {
        currentZoomLevel = mAMap.getCameraPosition().zoom;
        AMapGeoPoint amapGeoPoint = new AMapGeoPoint(geoPoint.getLatitudeE6()/1e6, geoPoint.getLongitudeE6()/1e6);
        LatLng latLng = new LatLng(amapGeoPoint.getLatitude(), amapGeoPoint.getLatitude());
        changeCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, currentZoomLevel, 0, 0)), null);
    }


    @Override
    public void setZoomLevel(float zoomLevel) {
        currentZoomLevel =zoomLevel;
        changeCamera(CameraUpdateFactory.zoomTo(currentZoomLevel), null);
    }

    @Override
    public void clear() {
        mAMap.clear(true);
    }

    @Override
    public void setMyLocationEnabled(boolean enable) {
        mAMap.setMyLocationEnabled(enable);
        mAMap.getUiSettings().setMyLocationButtonEnabled(enable);
        if (enable){
            mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        }

    }

    private void changeCamera(CameraUpdate update, AMap.CancelableCallback callback) {
        mAMap.animateCamera(update, 1000, callback);
    }



    /**
     * get MarkerOptions from cache
     * @return
     */
    private MarkerOptions getMarkerOptions(int iconInfo, int optionsSetState){
        return null;
    }
}
