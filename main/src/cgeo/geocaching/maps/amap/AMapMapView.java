package cgeo.geocaching.maps.amap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.MapView;

import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.location.Viewport;
import cgeo.geocaching.maps.CachesOverlay;
import cgeo.geocaching.maps.PositionAndScaleOverlay;
import cgeo.geocaching.maps.interfaces.GeneralOverlay;
import cgeo.geocaching.maps.interfaces.GeoPointImpl;
import cgeo.geocaching.maps.interfaces.MapControllerImpl;
import cgeo.geocaching.maps.interfaces.MapProjectionImpl;
import cgeo.geocaching.maps.interfaces.MapViewImpl;
import cgeo.geocaching.maps.interfaces.OnMapDragListener;

/**
 * Created by paint on 7/20/16.
 */
public class AMapMapView extends MapView implements MapViewImpl{



    public AMapMapView(Context context) {
        super(context);
    }

    public AMapMapView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public AMapMapView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public AMapMapView(Context context, AMapOptions aMapOptions) {
        super(context, aMapOptions);
    }

    @Override
    public void setBuiltInZoomControls(boolean b) {

    }

    @Override
    public void displayZoomControls(boolean b) {

    }

    @Override
    public void preLoad() {

    }

    @Override
    public void clearOverlays() {

    }

    @Override
    public MapControllerImpl getMapController() {
        return null;
    }

    @Override
    public GeoPointImpl getMapViewCenter() {
        return null;
    }

    @Override
    public int getLatitudeSpan() {
        return 0;
    }

    @Override
    public int getLongitudeSpan() {
        return 0;
    }

    @Override
    public int getMapZoomLevel() {
        return 0;
    }

    @Override
    public MapProjectionImpl getMapProjection() {
        return null;
    }

    @Override
    public CachesOverlay createAddMapOverlay(Context context, Drawable drawable) {
        return null;
    }

    @Override
    public PositionAndScaleOverlay createAddPositionAndScaleOverlay(Geopoint coords, String geocode) {
        return null;
    }

    @Override
    public void setMapSource() {

    }

    @Override
    public void repaintRequired(GeneralOverlay overlay) {

    }

    @Override
    public void setOnDragListener(OnMapDragListener onDragListener) {

    }

    @Override
    public boolean needsInvertedColors() {
        return false;
    }

    @Override
    public Viewport getViewport() {
        return null;
    }

    @Override
    public boolean hasMapThemes() {
        return false;
    }

    @Override
    public void setMapTheme() {

    }
}
