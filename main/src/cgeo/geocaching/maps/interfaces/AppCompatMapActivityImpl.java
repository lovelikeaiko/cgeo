package cgeo.geocaching.maps.interfaces;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by paint on 7/30/16.
 */
public interface AppCompatMapActivityImpl {

    Resources getResources();

    AppCompatActivity getActivity();

    void superOnResume();

    void superOnDestroy();

    void superOnStop();

    void superOnPause();

    void onMapSourceChanged(int oldSource, int newSource);


}
