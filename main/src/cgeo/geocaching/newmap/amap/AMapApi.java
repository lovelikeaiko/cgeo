package cgeo.geocaching.newmap.amap;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.VisibleRegion;

import java.util.ArrayList;
import java.util.List;

import cgeo.geocaching.CgeoApplication;
import cgeo.geocaching.location.Viewport;
import cgeo.geocaching.maps.amap.AMapGeoPoint;
import cgeo.geocaching.maps.interfaces.GeoPointImpl;
import cgeo.geocaching.maps.interfaces.MapProjectionImpl;
import cgeo.geocaching.newmap.BaseMapApi;
import cgeo.geocaching.newmap.data.BitmapDescriptor;
import cgeo.geocaching.newmap.data.MarkerOptionsInfo;
import cgeo.geocaching.newmap.interfaces.BuildMarkerOpionInfoCallback;
import cgeo.geocaching.newmap.interfaces.MapApiImpl;

/**
 * Created by paint on 17-5-22.
 */

public abstract class AMapApi extends BaseMapApi implements LocationSource,
        AMapLocationListener {


    private final float DEFAULT_ZOOM_LEVEL = 12;

    private float currentZoomLevel = DEFAULT_ZOOM_LEVEL;

    private SparseArray<MarkerOptions> mMarkerOptionsCache;

    private AMap mAMap = null;

    private MarkerOptions mTempMarkerOptions;

    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;

    public AMapApi(AMap map) {
        mAMap = map;
        mMarkerOptionsCache = new SparseArray<>();
        mAMap.setLocationSource(this);// 设置定位监听
        mAMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        mAMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        mAMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                notifyOnMapMoved(new AMapGeoPoint(cameraPosition.target.latitude, cameraPosition.target.longitude));
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {

            }
        });
    }



    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(CgeoApplication.getInstance());
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    public Viewport getViewport() {
        return null;
    }

    @Override
    public int getLatitudeSpan() {
        VisibleRegion vr = mAMap.getProjection().getVisibleRegion();
        LatLng leftTop = vr.farLeft;
        LatLng rightBottom = vr.nearRight;
        return (int)Math.abs(rightBottom.latitude-leftTop.latitude);
    }

    @Override
    public int getLongitudeSpan() {
        VisibleRegion vr = mAMap.getProjection().getVisibleRegion();
        LatLng leftTop = vr.farLeft;
        LatLng rightBottom = vr.nearRight;
        return (int)Math.abs(rightBottom.latitude-leftTop.latitude);
    }

    @Override
    public MapProjectionImpl getMapProjection() {
        return null;
    }

    @Override
    public GeoPointImpl getMapViewCenter() {
        VisibleRegion vr = mAMap.getProjection().getVisibleRegion();
        LatLng leftTop = vr.farLeft;
        LatLng rightBottom = vr.nearRight;
        return new AMapGeoPoint((leftTop.latitude+rightBottom.latitude)/2, (leftTop.longitude+rightBottom.longitude)/2);
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

//    @Override
//    public void addMarkerInMainThread(MarkerOptionsInfo info) {
////        MarkerOptions markerOptions = getMarkerOptions(info.getIconInfo(), info.getOptionsSetState());
////        if (markerOptions==null){
////            info.buildMarkerOpionInfo(mBuildCallback);
////        }
////        mAMap.addMarker(mTempMarkerOptions);
//    }

    @Override
    protected void addMarkerInfoInMainThread(MarkerOptionsInfo info) {

    }

    @Override
    protected void addMarkerInfoListInMainThread(List<MarkerOptionsInfo> infoList) {
        MarkerOptions markerOptions = null;
        for (BitmapDescriptor b : markers) {
            markerOptions = getMarkerOptions(b.hashCode());
            if (markerOptions==null){
                markerOptions = new MarkerOptions();
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(b.getBitmap()));
                mMarkerOptionsCache.put(b.hashCode(), markerOptions);
            }
            mAMap.addMarker(markerOptions);
        }
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
    public float getZoomLevel() {
        return currentZoomLevel;
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

    /**
     * get MarkerOptions from cache
     * @return
     */
    private MarkerOptions getMarkerOptions(int hashcode){
        return mMarkerOptionsCache.get(hashcode);
    }
}
