package cgeo.geocaching.newmap;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import cgeo.geocaching.newmap.data.MarkerOptionsInfo;
import cgeo.geocaching.newmap.interfaces.MapApiImpl;

/**
 * Created by Paint on 2017/5/23.
 */

public abstract class BaseMapApi implements MapApiImpl {

    private Handler mMainThreadHandler = null;

    private static final int ADD_MARKER = 0;

    class MapHandler extends Handler{
        public MapHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ADD_MARKER:
                    addMarkerInMainThread((MarkerOptionsInfo)msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    public BaseMapApi() {
        mMainThreadHandler = new MapHandler(Looper.getMainLooper());
    }

    protected abstract void addMarkerInMainThread(MarkerOptionsInfo info);

    @Override
    final public void addMarker(MarkerOptionsInfo info) {
        mMainThreadHandler.sendMessage(Message.obtain(null, ADD_MARKER, info));
    }
}
