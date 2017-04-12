package cgeo.geocaching.maps;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import cgeo.geocaching.R;
import cgeo.geocaching.maps.interfaces.AppCompatMapActivityImpl;

/**
 * Created by paint on 7/30/16.
 */
public abstract class AppCompatAbstractMap {

    final AppCompatMapActivityImpl mapActivity;

    protected AppCompatAbstractMap(final AppCompatMapActivityImpl activity) {
        mapActivity = activity;
    }

    public Resources getResources() {
        return mapActivity.getResources();
    }

    public AppCompatActivity getActivity() {
        return mapActivity.getActivity();
    }

    public void onResume() {
        mapActivity.superOnResume();
    }

    public void onStop() {
        mapActivity.superOnStop();
    }

    public void onPause() {
        mapActivity.superOnPause();
    }

    public void onDestroy() {
        mapActivity.superOnDestroy();
    }

//    public boolean onCreateOptionsMenu(final Menu menu) {
//        final boolean result = mapActivity.superOnCreateOptionsMenu(menu);
//        mapActivity.getActivity().getMenuInflater().inflate(R.menu.map_activity, menu);
//        return result;
//    }
//
//    public boolean onPrepareOptionsMenu(final Menu menu) {
//        return mapActivity.superOnPrepareOptionsMenu(menu);
//    }
//
//    public boolean onOptionsItemSelected(final MenuItem item) {
//        return mapActivity.superOnOptionsItemSelected(item);
//    }

    public abstract void onCreate(final Bundle savedInstanceState);

    public abstract void onSaveInstanceState(final Bundle outState);


    public abstract void onLocationChanged(double lat, double lon);
}
