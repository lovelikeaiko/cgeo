package cgeo.geocaching.newmap.interfaces;

import java.util.ArrayList;

import cgeo.geocaching.newmap.data.BitmapDescriptor;
import cgeo.geocaching.newmap.data.LatLng;
import cgeo.geocaching.newmap.data.MarkerOptionsInfo;

/**
 * Created by paint on 17-5-22.
 */

public interface BuildMarkerOpionInfoCallback {

    void onBuildStart(MarkerOptionsInfo info);

    void onBuildEnd(MarkerOptionsInfo info);

    public void icons(ArrayList<BitmapDescriptor> icons);


    public void period(int period);

    /** @deprecated */
    public void perspective(boolean perspective);


    public void position(LatLng position);

    public void setFlat(boolean flat);

    /**
     * 设置Marker覆盖物的锚点比例。
     * @param u
     * @param v
     */
    public void anchor(float u, float v);

    /**
     * 设置Marker覆盖物的InfoWindow相对Marker的偏移。
     * @param offsetX
     * @param offsetY
     */
    public void setInfoWindowOffset(int offsetX, int offsetY);

    public void title(String title);

    public void snippet(String snippet);

    public void draggable(boolean draggable) ;

    public void visible(boolean visible);

    public void setGps(boolean gps);

    public void zIndex(float zIndex);

    public void alpha(float alpha);

    public void displayLevel(int displayLevel);

    public void rotateAngle(float rotateAngle) ;

    public void infoWindowEnable(boolean infoWindowEnable);

}
