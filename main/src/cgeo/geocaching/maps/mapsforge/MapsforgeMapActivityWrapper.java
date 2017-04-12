package cgeo.geocaching.maps.mapsforge;

import android.view.View;

import org.mapsforge.v3.android.maps.MapActivity;

/**
 * Created by paint on 7/18/16.
 */
public class MapsforgeMapActivityWrapper extends MapActivity{

    public MapsforgeMapActivityWrapper(){
        super();
    }

    public void onDestroy(){
        super.onDestroy();
    }

    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
    }

    public View inflate(int layoutID){
        return  getLayoutInflater().inflate(layoutID, null);
    }
}
