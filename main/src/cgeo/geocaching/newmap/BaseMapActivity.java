package cgeo.geocaching.newmap;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import cgeo.geocaching.R;
import cgeo.geocaching.maps.AppCompatAbstractMap;
import cgeo.geocaching.maps.interfaces.AppCompatMapActivityImpl;
import cgeo.geocaching.maps.interfaces.MapProvider;
import cgeo.geocaching.newmap.interfaces.MapApiImpl;
import cgeo.geocaching.newmap.interfaces.OnMapReadyCallback;
import cgeo.geocaching.settings.Settings;
import cgeo.geocaching.utils.Log;

/**
 * Created by paint on 17-5-22.
 */

public abstract class BaseMapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        AppCompatMapActivityImpl, OnMapReadyCallback {

    private final AppCompatAbstractMap mapBase;

    private FrameLayout mapContainer;

    MapProvider mapProvider;

    private MapApiImpl mapApi;

    public BaseMapActivity() {
        mapBase = new NewCGeoMap(this);
    }

    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    // TODO what should I do here?
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        mapBase.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mapContainer = (FrameLayout) findViewById(R.id.map_container);
        if (mapContainer != null) {
            mapProvider = Settings.getMapProvider();
            Fragment mapFragment = mapProvider.getFragmentFactory().createFragment();
            if (mapFragment != null) {
                addFragment(R.id.map_container, mapFragment);
                mapProvider.getFragmentFactory().getMapAsync(mapFragment);
            }
        }
        mapBase.onCreate(savedInstanceState);

    }

    @Override
    public void onMapReady() {
        mapApi = mapProvider.createMapApi();
    }

    @Override
    public void onMapSourceChanged(int oldSource, int newSource) {

    }

    private void addFragment(int containerViewId, Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(containerViewId, fragment);
        ft.commitAllowingStateLoss();
    }

    private void replaceFragment(int containerViewId, Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(containerViewId, fragment);
        ft.commitAllowingStateLoss();
    }

//    /**
//     * 子类为android:id="@+id/map_container"添加fragment
//     */
//    protected abstract Fragment getMapFragment();

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ac_main_activity_menu, menu);

        // TODO difference for different maps
//        mapBase.onCreateOptionsMenu(menu);
        return true;
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
    protected void onStop() {
        mapBase.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_item_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_find_geocaches) {
            // Handle the camera action
        } else if (id == R.id.nav_messages) {

        } else if (id == R.id.nav_my_list) {

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_log_out) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void superOnDestroy() {
        super.onDestroy();

    }

    @Override
    public void superOnResume() {
        super.onResume();
    }

    @Override
    public void superOnStop() {
        super.onStop();
    }

    @Override
    public void superOnPause() {
        super.onPause();
    }
}
