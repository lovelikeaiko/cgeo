package cgeo.geocaching.newmap.data;


import java.util.ArrayList;

import cgeo.geocaching.newmap.data.BitmapDescriptor;
import cgeo.geocaching.newmap.data.LatLng;
import cgeo.geocaching.newmap.interfaces.BuildMarkerOpionInfoCallback;

/**
 * Created by paint on 17-5-22.
 */

public class MarkerOptionsInfo {

    private ArrayList<BitmapDescriptor> icons = new ArrayList();

    /**
     * 表示icon是由那些图标组成，缓存判别标识
     */
    private int iconInfo;

    private int period;
    private boolean perspective;
    private LatLng position;
    boolean flat;
    float u;
    float v;
    private int offsetX;
    private int offsetY;
    private String title;
    private String snippet;
    private boolean draggable;
    private boolean visible;
    private boolean gps;
    private float zIndex;
    private float alpha;
    private int displayLevel;
    private float rotateAngle;
    private boolean infoWindowEnable;



    private final static int SET_ICONS = 0;
    private final static int SET_PERIOD = 1;
    private final static int SET_PERSPECTIVE = 2;
    private final static int SET_POSITION = 3;
    private final static int SET_FLAT = 4;
    private final static int SET_U = 5;
    private final static int SET_V = 6;
    private final static int SET_OFFSET_X = 7;
    private final static int SET_OFFSET_Y = 8;
    private final static int SET_TITLE = 9;
    private final static int SET_SNIPPET = 10;
    private final static int SET_DRAGGABLE = 11;
    private final static int SET_VISIBLE = 12;
    private final static int SET_GPS = 13;
    private final static int SET_Z_INDEX = 14;
    private final static int SET_ALPHA = 15;
    private final static int SET_DISPALY_LEVEL = 16;
    private final static int SET_ROTATE_ANGLE = 17;
    private final static int SET_INFO_WINDOW_ENABLE = 18;

    public final static int FLAG_ICONS = 1;
    public final static int FLAG_PERIOD = 1<<SET_PERIOD;
    public final static int FLAG_PERSPECTIVE = 1<<SET_PERSPECTIVE;
    public final static int FLAG_POSITION = 1<<SET_POSITION;
    public final static int FLAG_FLAT = 1<<SET_FLAT;
    public final static int FLAG_U = 1<<SET_U;
    public final static int FLAG_V = 1<<SET_V;
    public final static int FLAG_OFFSET_X = 1<<SET_OFFSET_X;
    public final static int FLAG_OFFSET_Y = 1<<SET_OFFSET_Y;
    public final static int FLAG_TITLE = 1<<SET_TITLE;
    public final static int FLAG_SNIPPET = 1<<SET_SNIPPET;
    public final static int FLAG_DRAGGABLE = 1<<SET_DRAGGABLE;
    public final static int FLAG_VISIBLE = 1<<SET_VISIBLE;
    public final static int FLAG_GPS = 1<<SET_GPS;
    public final static int FLAG_Z_INDEX = 1<<SET_Z_INDEX;
    public final static int FLAG_ALPHA = 1<<SET_ALPHA;
    public final static int FLAG_DISPALY_LEVEL = 1<<SET_DISPALY_LEVEL;
    public final static int FLAG_ROTATE_ANGLE = 1<<SET_ROTATE_ANGLE;
    public final static int FLAG_INFO_WINDOW_ENABLE = 1<<SET_INFO_WINDOW_ENABLE;

    private int optionsSetState = 0;

    public int getOptionsSetState(){
        return optionsSetState;
    }

    private void setIconInfo(int iconInfo){
        this.iconInfo = iconInfo;
    }

    public int getIconInfo(){
        return iconInfo;
    }

    private void setState(int flag){
        optionsSetState |= flag;
    }

    public boolean isSet(int flag){
        return (optionsSetState & (~flag))==0;
    }

    public void icons(ArrayList<BitmapDescriptor> icons) {
        this.icons = icons;
        setState(FLAG_ICONS);
    }

    public ArrayList<BitmapDescriptor> getIcons() {
        return this.icons;
    }

    public void period(int period) {
        if(period <= 1) {
            this.period = 1;
        } else {
            this.period = period;
        }
        setState(FLAG_PERIOD);
    }

    public int getPeriod() {
        return this.period;
    }

    /** @deprecated */
    public boolean isPerspective() {
        return this.perspective;
    }

    /** @deprecated */
    public void perspective(boolean perspective) {
        this.perspective = perspective;
        setState(FLAG_PERSPECTIVE);
    }

    public MarkerOptionsInfo() {
//        icons = null;
//        period = 0;
//        perspective = false;
    }

    public void position(LatLng position) {
        this.position = position;
        setState(FLAG_POSITION);
    }

    public void setFlat(boolean flat) {
        this.flat = flat;
        setState(FLAG_FLAT);
    }

    private void initIconsArray() {
        if(this.icons == null) {
            this.icons = new ArrayList();
        }
    }

    public void icon(BitmapDescriptor icon) {
        try {
            this.initIconsArray();
            this.icons.clear();
            this.icons.add(icon);
            setState(FLAG_ICONS);
        } catch (Throwable var3) {
            var3.printStackTrace();
        }
    }

    /**
     * 设置Marker覆盖物的锚点比例。
     * @param u
     * @param v
     */
    public void anchor(float u, float v) {
        this.u = u;
        this.v = v;
        setState(FLAG_U);
        setState(FLAG_V);
    }

    /**
     * 设置Marker覆盖物的InfoWindow相对Marker的偏移。
     * @param offsetX
     * @param offsetY
     */
    public void setInfoWindowOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        setState(FLAG_OFFSET_X);
        setState(FLAG_OFFSET_Y);
    }

    public void title(String title) {
        this.title = title;
        setState(FLAG_TITLE);
    }

    public void snippet(String snippet) {
        this.snippet = snippet;
        setState(FLAG_SNIPPET);
    }

    public void draggable(boolean draggable) {
        this.draggable = draggable;
        setState(FLAG_DRAGGABLE);
    }

    public void visible(boolean visible) {
        this.visible = visible;
        setState(FLAG_VISIBLE);
    }

    public void setGps(boolean gps) {
        this.gps = gps;
        setState(FLAG_GPS);
    }

    public void zIndex(float zIndex) {
        this.zIndex = zIndex;
        setState(FLAG_Z_INDEX);
    }

    public void alpha(float alpha) {
        this.alpha = alpha;
        setState(FLAG_ALPHA);
    }

    public void displayLevel(int displayLevel) {
        this.displayLevel = displayLevel;
        setState(FLAG_DISPALY_LEVEL);
    }

    public void rotateAngle(float rotateAngle) {
        this.rotateAngle = rotateAngle;
        setState(FLAG_ROTATE_ANGLE);
    }

    public void infoWindowEnable(boolean infoWindowEnable) {
        this.infoWindowEnable = infoWindowEnable;
        setState(FLAG_INFO_WINDOW_ENABLE);
    }

    public LatLng getPosition() {
        return this.position;
    }

    public String getTitle() {
        return this.title;
    }

    public String getSnippet() {
        return this.snippet;
    }

    public BitmapDescriptor getIcon() {
        return this.icons != null && this.icons.size() != 0?(BitmapDescriptor)this.icons.get(0):null;
    }

    public float getAnchorU() {
        return this.u;
    }
    
    public float getAnchorV() {
        return this.v;
    }
    
    public int getInfoWindowOffsetX() {
        return this.offsetX;
    }

    public int getInfoWindowOffsetY() {
        return this.offsetY;
    }



    public boolean isDraggable() {
        return this.draggable;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isGps() {
        return this.gps;
    }

    public boolean isFlat() {
        return this.flat;
    }

    public float getZIndex() {
        return this.zIndex;
    }

    public float getAlpha() {
        return this.alpha;
    }

    public int getDisplayLevel() {
        return this.displayLevel;
    }
    public float getRotateAngle() {
        return this.rotateAngle;
    }
    public boolean isInfoWindowEnable() {
        return this.infoWindowEnable;
    }





    public void buildMarkerOpionInfo(BuildMarkerOpionInfoCallback callback){
        if(callback==null) return;

        if(isSet(FLAG_ICONS)){
            callback.icons(icons);
        }
        if(isSet(FLAG_PERIOD)){
            callback.period(period);
        }
        if(isSet(FLAG_PERSPECTIVE)){
            callback.perspective(perspective);
        }
        if(isSet(FLAG_POSITION)){
            callback.position(position);
        }
        if(isSet(FLAG_FLAT)){
            callback.setFlat(flat);
        }
        if(isSet(FLAG_U)){
            callback.anchor(u, v);
        }
        if(isSet(FLAG_OFFSET_X)){
            callback.setInfoWindowOffset(offsetX, offsetY);
        }
        if(isSet(FLAG_TITLE)){
            callback.title(title);
        }
        if(isSet(FLAG_SNIPPET)){
            callback.snippet(snippet);
        }
        if(isSet(FLAG_DRAGGABLE)){
            callback.draggable(draggable);
        }
        if(isSet(FLAG_VISIBLE)){
            callback.visible(visible);
        }
        if(isSet(FLAG_GPS)){
            callback.setGps(gps);
        }
        if(isSet(FLAG_Z_INDEX)){
            callback.zIndex(zIndex);
        }

        if(isSet(FLAG_ALPHA)){
            callback.alpha(alpha);
        }
        if(isSet(FLAG_DISPALY_LEVEL)){
            callback.displayLevel(displayLevel);
        }
        if(isSet(FLAG_ROTATE_ANGLE)){
            callback.rotateAngle(rotateAngle);
        }
        if(isSet(FLAG_INFO_WINDOW_ENABLE)){
            callback.infoWindowEnable(infoWindowEnable);
        }
    }

}
