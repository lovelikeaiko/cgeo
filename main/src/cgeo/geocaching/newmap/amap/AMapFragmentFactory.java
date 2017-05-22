package cgeo.geocaching.newmap.amap;

import android.support.v4.app.Fragment;

import com.amap.api.maps.SupportMapFragment;

import cgeo.geocaching.newmap.fragment.FragmentFactory;

/**
 * Created by paint on 17-5-22.
 */

public class AMapFragmentFactory extends FragmentFactory {

    @Override
    public Fragment createFragment() {
        return new AMapMapFragment();
    }

    @Override
    public void getMapAsync(Fragment mapFragment) {
        if (mapFragment instanceof SupportMapFragment){
            ((SupportMapFragment)mapFragment).get
        }
    }
}
