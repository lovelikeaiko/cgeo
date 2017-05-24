package cgeo.geocaching.newmap;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cgeo.geocaching.maps.interfaces.GeoPointImpl;
import cgeo.geocaching.newmap.data.BitmapDescriptor;
import cgeo.geocaching.newmap.data.MarkerOptionsInfo;
import cgeo.geocaching.newmap.interfaces.MapApiImpl;
import cgeo.geocaching.newmap.interfaces.MapApiListener;

/**
 * Created by Paint on 2017/5/23.
 */

public abstract class BaseMapApi implements MapApiImpl {

    private Handler mMainThreadHandler = null;

    private CopyOnWriteArrayList<MapApiListener> mListeners;

    private static final int ADD_MARKER_INFO = 0;
    private static final int ADD_MARKER_BITMAP_LIST = 1;
    private static final int ADD_MARKER_INFO_LIST = 2;


    class MapHandler extends Handler{
        public MapHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ADD_MARKER_INFO:
                    addMarkerInfoInMainThread((MarkerOptionsInfo)msg.obj);
                    break;
                case ADD_MARKER_INFO_LIST:
                    addMarkerInfoListInMainThread((List<MarkerOptionsInfo>)msg.obj);
                    break;
//                case ADD_MARKER_BITMAP_LIST:
////                    addMarkerInMainThread((List<BitmapDescriptor>)msg.obj);
//                    break;
                default:
                    break;
            }
        }
    }

    public BaseMapApi() {
        mMainThreadHandler = new MapHandler(Looper.getMainLooper());
        mListeners = new CopyOnWriteArrayList<>();
    }

    public void addMapApiListener(MapApiListener listener){
        if (!mListeners.contains(listener)){
            mListeners.add(listener);
        }
    }

    public void removeMapApiListener(MapApiListener listener){
        mListeners.remove(listener);
    }

    /**
     * 需要重新加载cache
     * @param center
     */
    protected void notifyOnMapMoved(GeoPointImpl center){
        for (MapApiListener listener : mListeners) {
            listener.onMapMoved(center);
        }
    }

    protected abstract void addMarkerInfoInMainThread(MarkerOptionsInfo info);

    protected abstract void addMarkerInfoListInMainThread(List<MarkerOptionsInfo> infoList);

//    protected abstract void addMarkerBitmapInMainThread(List<BitmapDescriptor> markers);

    @Override
    final public void addMarker(MarkerOptionsInfo info) {
        mMainThreadHandler.sendMessage(Message.obtain(null, ADD_MARKER_INFO, info));
    }
    @Override
    final public void addMarker(List<MarkerOptionsInfo> infoList) {
        mMainThreadHandler.sendMessage(Message.obtain(null, ADD_MARKER_INFO_LIST, infoList));
    }

//    @Override
//    public void addMarker(List<BitmapDescriptor> markers) {
//        mMainThreadHandler.sendMessage(Message.obtain(null, ADD_MARKER_BITMAP_LIST, markers));
//    }
}
