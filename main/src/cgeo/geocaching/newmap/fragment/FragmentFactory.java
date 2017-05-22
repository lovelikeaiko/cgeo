package cgeo.geocaching.newmap.fragment;

import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import cgeo.geocaching.newmap.interfaces.OnMapReadyCallback;

/**
 * Created by paint on 17-5-22.
 */

public abstract class FragmentFactory {

    public abstract Fragment createFragment();

    public abstract void getMapAsync(Fragment mapFragment);

    public CopyOnWriteArrayList<OnMapReadyCallback> mOnMapReadyCallbacks = new CopyOnWriteArrayList<>();

    public void addOnMapReadyCallback(OnMapReadyCallback callback){
        if (!mOnMapReadyCallbacks.contains(callback)){
            mOnMapReadyCallbacks.add(callback);
        }
    }

    public void removeOnMapReadyCallback(OnMapReadyCallback callback){
        mOnMapReadyCallbacks.remove(callback);
    }
}
