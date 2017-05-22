package cgeo.geocaching.newmap.google;

import android.support.v4.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import cgeo.geocaching.newmap.fragment.FragmentFactory;

/**
 * Created by paint on 17-5-22.
 */

public class GoogleMapFragmentFactory extends FragmentFactory {

    GoogleMap mGoogleMap = null;

    @Override
    public Fragment createFragment() {
        return new GoogleMapFragment();
    }

    public GoogleMap getGoogleMap() {
        return mGoogleMap;
    }

    private OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;
            notifyOnMapReadyCallback();
        }
    };

    @Override
    public void getMapAsync(Fragment mapFragment) {
        if (mapFragment instanceof SupportMapFragment){
            ((SupportMapFragment) mapFragment).getMapAsync(onMapReadyCallback);
        }
    }
}
