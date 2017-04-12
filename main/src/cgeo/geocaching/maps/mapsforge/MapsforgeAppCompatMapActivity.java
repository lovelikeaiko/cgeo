package cgeo.geocaching.maps.mapsforge;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import cgeo.geocaching.activity.ActivityMixin;
import cgeo.geocaching.activity.FilteredActivity;
import cgeo.geocaching.maps.AbstractMap;
import cgeo.geocaching.maps.CGeoMap;
import cgeo.geocaching.maps.interfaces.AppCompatLayoutInflater;
import cgeo.geocaching.maps.interfaces.MapActivityImpl;

/**
 * Created by paint on 7/18/16.
 */
public class MapsforgeAppCompatMapActivity extends AppCompatActivity implements MapActivityImpl, FilteredActivity, AppCompatLayoutInflater {

    private final AbstractMap mapBase;
    private final MapsforgeMapActivityWrapper mapActivityWrapper;

    public MapsforgeAppCompatMapActivity(){
        mapBase = new CGeoMap(this);
        mapActivityWrapper = new MapsforgeMapActivityWrapper();
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    protected void onCreate(final Bundle icicle) {
        mapBase.onCreate(icicle);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        mapBase.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        mapBase.onDestroy();
    }

    @Override
    protected void onPause() {
        mapBase.onPause();
    }

    @Override
    protected void onResume() {
        mapBase.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        return mapBase.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        return mapBase.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        return mapBase.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onStop() {
        mapBase.onStop();
    }

    @Override
    public void superOnCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean superOnCreateOptionsMenu(final Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void superOnDestroy() {
        super.onDestroy();
        mapActivityWrapper.onDestroy();
    }

    @Override
    public boolean superOnOptionsItemSelected(final MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void superOnResume() {
        super.onResume();
        mapActivityWrapper.onResume();
    }

    @Override
    public void superOnStop() {
        super.onStop();
    }

    @Override
    public void superOnPause() {
        super.onPause();
        mapActivityWrapper.onPause();
    }

    @Override
    public boolean superOnPrepareOptionsMenu(final Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void navigateUp(final View view) {
        ActivityMixin.navigateUp(this);
    }

    @Override
    public void showFilterMenu(final View view) {
        // do nothing, the filter bar only shows the global filter
    }

    @Override
    public View inflate(int layoutID) {
        return mapActivityWrapper.inflate(layoutID);
    }

}
