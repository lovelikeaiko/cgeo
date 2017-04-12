package cgeo.geocaching.maps;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.Projection;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.amap.api.maps.model.VisibleRegion;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import cgeo.geocaching.CacheListActivity;
import cgeo.geocaching.CompassActivity;
import cgeo.geocaching.Intents;
import cgeo.geocaching.R;
import cgeo.geocaching.SearchResult;
import cgeo.geocaching.activity.ActivityMixin;
import cgeo.geocaching.compatibility.Compatibility;
import cgeo.geocaching.connector.ConnectorFactory;
import cgeo.geocaching.connector.gc.GCLogin;
import cgeo.geocaching.connector.gc.MapTokens;
import cgeo.geocaching.connector.gc.Tile;
import cgeo.geocaching.enumerations.CacheType;
import cgeo.geocaching.enumerations.LoadFlags;
import cgeo.geocaching.enumerations.LoadFlags.RemoveFlag;
import cgeo.geocaching.enumerations.WaypointType;
import cgeo.geocaching.list.StoredList;
import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.location.Viewport;
import cgeo.geocaching.maps.amap.AMapGeoPoint;
import cgeo.geocaching.maps.interfaces.AppCompatLayoutInflater;
import cgeo.geocaching.maps.interfaces.CachesOverlayItemImpl;
import cgeo.geocaching.maps.interfaces.GeoPointImpl;
import cgeo.geocaching.maps.interfaces.AppCompatMapActivityImpl;
import cgeo.geocaching.maps.interfaces.MapControllerImpl;
import cgeo.geocaching.maps.interfaces.MapItemFactory;
import cgeo.geocaching.maps.interfaces.MapProvider;
import cgeo.geocaching.maps.interfaces.MapSource;
import cgeo.geocaching.maps.interfaces.MapViewImpl;
import cgeo.geocaching.maps.interfaces.OnMapDragListener;
import cgeo.geocaching.models.Geocache;
import cgeo.geocaching.models.Waypoint;
import cgeo.geocaching.network.AndroidBeam;
import cgeo.geocaching.sensors.GeoData;
import cgeo.geocaching.sensors.GeoDirHandler;
import cgeo.geocaching.sensors.Sensors;
import cgeo.geocaching.settings.Settings;
import cgeo.geocaching.storage.DataStore;
import cgeo.geocaching.utils.AndroidRxUtils;
import cgeo.geocaching.utils.AngleUtils;
import cgeo.geocaching.utils.CancellableHandler;
import cgeo.geocaching.utils.Formatter;
import cgeo.geocaching.utils.GpsCorrector;
import cgeo.geocaching.utils.JZLocationConverter;
import cgeo.geocaching.utils.LeastRecentlyUsedSet;
import cgeo.geocaching.utils.Log;
import cgeo.geocaching.utils.MapUtils;
import cgeo.geocaching.utils.TransformUtil;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

import cgeo.geocaching.maps.CGeoMap.MapMode;

/**
 * Class representing the Map in c:geo for appcompat
 */
public class CGeoMapAppCompat extends AppCompatAbstractMap implements ViewFactory {

    public static final String TAG = "CGeoMap";

    /** max. number of caches displayed in the Live Map */
    public static final int MAX_CACHES = 500;
    /**
     * initialization with an empty subscription to make static code analysis tools more happy
     */
    private Subscription resumeSubscription = Subscriptions.empty();


    /** Handler Messages */
    private static final int HIDE_PROGRESS = 0;
    private static final int SHOW_PROGRESS = 1;
    private static final int UPDATE_TITLE = 0;
    private static final int INVALIDATE_MAP = 1;
    private static final int UPDATE_PROGRESS = 0;
    private static final int FINISHED_LOADING_DETAILS = 1;

    private static final String BUNDLE_MAP_SOURCE = "mapSource";
    private static final String BUNDLE_MAP_STATE = "mapState";
    private static final String BUNDLE_LIVE_ENABLED = "liveEnabled";
    private static final String BUNDLE_TRAIL_HISTORY = "trailHistory";

    // Those are initialized in onCreate() and will never be null afterwards
    private Resources res;
    private AppCompatActivity activity;
    private MapItemFactory mapItemFactory;
    private String mapTitle;
    private final LeastRecentlyUsedSet<Geocache> caches = new LeastRecentlyUsedSet<>(MAX_CACHES + DataStore.getAllCachesCount());
    private MapViewImpl mapView;
    private CachesOverlay overlayCaches;
    private PositionAndScaleOverlay overlayPositionAndScale;

    private final GeoDirHandler geoDirUpdate = new UpdateLoc(this);
    private SearchResult searchIntent = null;
    private String geocodeIntent = null;
    private Geopoint coordsIntent = null;
    private WaypointType waypointTypeIntent = null;
    private int[] mapStateIntent = null;
    // status data
    /** Last search result used for displaying header */
    private SearchResult lastSearchResult = null;
    private MapTokens tokens = null;
    private boolean noMapTokenShowed = false;
    // map status data
    private static boolean followMyLocation = true;
    // threads
    private Subscription loadTimer;
    private LoadDetails loadDetailsThread = null;
    /** Time of last {@link LoadRunnable} run */
    private volatile long loadThreadRun = 0L;
    //Interthread communication flag
    private volatile boolean downloaded = false;

    /** Count of caches currently visible */
    private int cachesCnt = 0;
    /** List of waypoints in the viewport */
    private final LeastRecentlyUsedSet<Waypoint> waypoints = new LeastRecentlyUsedSet<>(MAX_CACHES);
    // storing for offline
    private ProgressDialog waitDialog = null;
    private int detailTotal = 0;
    private int detailProgress = 0;
    private long detailProgressTime = 0L;

    // views
    private CheckBox myLocSwitch = null;
    /** Controls the map behavior */
    private MapMode mapMode = null;
    /** Live mode enabled for map. **/
    private boolean isLiveEnabled;
    // other things
    private boolean markersInvalidated = false; // previous state for loadTimer
    private boolean centered = false; // if map is already centered
    private boolean alreadyCentered = false; // -""- for setting my location
    private static final Set<String> dirtyCaches = new HashSet<>();
    // flag for honeycomb special popup menu handling
    private boolean honeycombMenu = false;

    /**
     * if live map is enabled, this is the minimum zoom level, independent of the stored setting
     */
    private static final int MIN_LIVEMAP_ZOOM = 12;
    // Thread pooling
    private static final BlockingQueue<Runnable> displayQueue = new ArrayBlockingQueue<>(1);
    private static final ThreadPoolExecutor displayExecutor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, displayQueue, new ThreadPoolExecutor.DiscardOldestPolicy());
    private static final BlockingQueue<Runnable> downloadQueue = new ArrayBlockingQueue<>(1);
    private static final ThreadPoolExecutor downloadExecutor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, downloadQueue, new ThreadPoolExecutor.DiscardOldestPolicy());
    private static final BlockingQueue<Runnable> loadQueue = new ArrayBlockingQueue<>(1);
    private static final ThreadPoolExecutor loadExecutor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, loadQueue, new ThreadPoolExecutor.DiscardOldestPolicy());
    // handlers
    /** Updates the titles */

    private static final class DisplayHandler extends Handler {

        private final WeakReference<CGeoMapAppCompat> mapRef;

        DisplayHandler(@NonNull final CGeoMapAppCompat map) {
            this.mapRef = new WeakReference<>(map);
        }
        @Override
        public void handleMessage(final Message msg) {
            final int what = msg.what;
            final CGeoMapAppCompat map = mapRef.get();
            if (map == null) {
                return;
            }

            switch (what) {
                case UPDATE_TITLE:
                    map.setTitle();
                    map.setSubtitle();

                    break;
                case INVALIDATE_MAP:
                    map.mapView.repaintRequired(null);
                    break;

                default:
                    break;
            }
        }

    }

    private final Handler displayHandler = new DisplayHandler(this);

    private void setTitle() {
        final String title = calculateTitle();
        /* Compatibility for the old Action Bar, only used by the maps activity at the moment */
        final TextView titleview = ButterKnife.findById(activity, R.id.actionbar_title);
        if (titleview != null) {
            titleview.setText(title);

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setTitleIceCreamSandwich(title);
        }
    }

    private String calculateTitle() {
        if (isLiveEnabled) {
            return res.getString(R.string.map_live);
        }
        if (mapMode == MapMode.SINGLE) {
            final Geocache cache = getSingleModeCache();
            if (cache != null) {
                return cache.getName();
            }
        }
        return StringUtils.defaultIfEmpty(mapTitle, res.getString(R.string.map_map));
    }

    @Nullable
    private Geocache getSingleModeCache() {
        // use a copy of the caches list to avoid concurrent modification
        for (final Geocache geocache : caches.getAsList()) {
            if (geocache.getGeocode().equals(geocodeIntent)) {
                return geocache;
            }
        }
        return null;
    }

    private void setSubtitle() {
        final String subtitle = calculateSubtitle();
        if (StringUtils.isEmpty(subtitle)) {
            return;
        }

        /* Compatibility for the old Action Bar, only used by the maps activity at the moment */
        final TextView titleView = ButterKnife.findById(activity, R.id.actionbar_title);
        if (titleView != null) {
            titleView.setText(titleView.getText().toString() + ' ' + subtitle);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setSubtitleIceCreamSandwich(subtitle);
        }
    }

    private String calculateSubtitle() {
        // count caches in the sub title
        countVisibleCaches();
        final StringBuilder subtitle = new StringBuilder();
        if (!isLiveEnabled && mapMode == MapMode.SINGLE) {
            final Geocache cache = getSingleModeCache();
            if (cache != null) {
                return Formatter.formatMapSubtitle(cache);
            }
        }
        if (!caches.isEmpty()) {
            final int totalCount = caches.size();

            if (cachesCnt != totalCount && Settings.isDebug()) {
                subtitle.append(cachesCnt).append('/').append(res.getQuantityString(R.plurals.cache_counts, totalCount, totalCount));
            } else {
                subtitle.append(res.getQuantityString(R.plurals.cache_counts, cachesCnt, cachesCnt));
            }
        }

        if (Settings.isDebug() && lastSearchResult != null && StringUtils.isNotBlank(lastSearchResult.getUrl())) {
            subtitle.append(" [").append(lastSearchResult.getUrl()).append(']');
        }

        return subtitle.toString();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @NonNull
    private ActionBar getActionBar() {
        final ActionBar actionBar = activity.getActionBar();
        assert actionBar != null;
        return actionBar;
    }

    private boolean isAppCompatActivity(){
        if(activity instanceof AppCompatActivity){
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setTitleIceCreamSandwich(final String title) {

        if(isAppCompatActivity()){
            ((AppCompatActivity)activity).getSupportActionBar().setTitle(title);
        }else{
            getActionBar().setTitle(title);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setSubtitleIceCreamSandwich(final String subtitle) {
        if(isAppCompatActivity()){
            ((AppCompatActivity)activity).getSupportActionBar().setSubtitle(subtitle);
        }else{
            getActionBar().setSubtitle(subtitle);
        }
    }

    /** Updates the progress. */
    private static final class ShowProgressHandler extends Handler {
        private int counter = 0;

        @NonNull private final WeakReference<CGeoMapAppCompat> mapRef;

        ShowProgressHandler(@NonNull final CGeoMapAppCompat map) {
            this.mapRef = new WeakReference<>(map);
        }

        @Override
        public void handleMessage(final Message msg) {
            final int what = msg.what;

            if (what == HIDE_PROGRESS) {
                if (--counter == 0) {
                    showProgress(false);
                }
            } else if (what == SHOW_PROGRESS) {
                showProgress(true);
                counter++;
            }
        }
        private void showProgress(final boolean show) {
            final CGeoMapAppCompat map = mapRef.get();
            if (map == null) {
                return;
            }

            final ProgressBar progress = ButterKnife.findById(map.activity, R.id.actionbar_progress);

            Log.e("progress=" + progress);

            if (progress != null) {
                if (show) {
                    progress.setVisibility(View.VISIBLE);
                } else {
                    progress.setVisibility(View.GONE);

                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                map.activity.setProgressBarIndeterminateVisibility(show);
            }
        }

    }

    private final Handler showProgressHandler = new ShowProgressHandler(this);

    private final class LoadDetailsHandler extends CancellableHandler {

        @Override
        public void handleRegularMessage(final Message msg) {
            if (msg.what == UPDATE_PROGRESS) {
                if (waitDialog != null) {
                    final int secondsElapsed = (int) ((System.currentTimeMillis() - detailProgressTime) / 1000);
                    final int secondsRemaining;
                    if (detailProgress > 0) {
                        secondsRemaining = (detailTotal - detailProgress) * secondsElapsed / detailProgress;
                    } else {
                        secondsRemaining = (detailTotal - detailProgress) * secondsElapsed;
                    }

                    waitDialog.setProgress(detailProgress);
                    if (secondsRemaining < 40) {
                        waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + res.getString(R.string.caches_eta_ltm));
                    } else {
                        final int minsRemaining = secondsRemaining / 60;
                        waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + res.getQuantityString(R.plurals.caches_eta_mins, minsRemaining, minsRemaining));
                    }
                }
            } else if (msg.what == FINISHED_LOADING_DETAILS && waitDialog != null) {
                waitDialog.dismiss();
                waitDialog.setOnCancelListener(null);
            }
        }
        @Override
        public void handleCancel(final Object extra) {
            if (loadDetailsThread != null) {
                loadDetailsThread.stopIt();
            }
        }

    }

    /* Current source id */
    private int currentSourceId;

    public CGeoMapAppCompat(final AppCompatMapActivityImpl activity) {
        super(activity);
    }

    protected void countVisibleCaches() {
//        cachesCnt = mapView.getViewport().count(caches.getAsList());

        if(caches!=null){
            cachesCnt = caches.size();
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putInt(BUNDLE_MAP_SOURCE, currentSourceId);
        outState.putIntArray(BUNDLE_MAP_STATE, currentMapState());
        outState.putBoolean(BUNDLE_LIVE_ENABLED, isLiveEnabled);
        outState.putParcelableArrayList(BUNDLE_TRAIL_HISTORY, overlayPositionAndScale.getHistory());

        mAmapMapView.onSaveInstanceState(outState);
    }

    //TODO temp add
//    SupportMapFragment amapFragment;
    private AMap mAMap;
    private MapView mAmapMapView;

    private MainThreadHandler mMainThreadHandler;

    private final static int MAIN_THREAD_ADD_MARKERS = 1;
    private final static int MAIN_THREAD_TIMER = 2;
    private final static int MAIN_THREAD_LOAD = 3;

    public class MainThreadHandler extends Handler{

        public MainThreadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case MAIN_THREAD_ADD_MARKERS:
                    if (msg.obj==null) return;

                    if(msg.obj instanceof ArrayList){
                        ArrayList<MarkerOptions> markerOptionsesToDisplay = (ArrayList<MarkerOptions>)msg.obj;
                        mAMap.addMarkers(markerOptionsesToDisplay, false);
                    }

                    if(msg.obj instanceof TextOptions){
                        mAMap.addText((TextOptions) msg.obj);
                    }
                    break;

                case MAIN_THREAD_TIMER:

                    Log.w("MAIN_THREAD_TIMER, latspan=" + getLatitudeSpan() + ", lonspan=" + getLongitudeSpan()
                        + ", visibleregion=" + getVisibleRegion());

                    sendEmptyMessageDelayed(MAIN_THREAD_TIMER, 2000);
                    break;

                case MAIN_THREAD_LOAD:
                    Log.w("MAIN_THREAD_LOAD, latspan=" + getLatitudeSpan() + ", lonspan=" + getLongitudeSpan()
                            + ", visibleregion=" + getVisibleRegion());

                    if (getLatitudeSpan()<900000){
                        loadExecutor.execute(new LoadRunnable(CGeoMapAppCompat.this));
                    }else{
                        sendEmptyMessageDelayed(MAIN_THREAD_LOAD, 2000);
                    }

                    break;

                default:

                    break;

            }


        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        // class init
        res = this.getResources();
        activity = this.getActivity();


        mMainThreadHandler = new MainThreadHandler(activity.getMainLooper());

        final MapProvider mapProvider = Settings.getMapProvider();
//        mapItemFactory = mapProvider.getMapItemFactory();

        // Get parameters from the intent
        final Bundle extras = activity.getIntent().getExtras();
        if (extras != null) {
            mapMode = (MapMode) extras.get(Intents.EXTRA_MAP_MODE);
            isLiveEnabled = extras.getBoolean(Intents.EXTRA_LIVE_ENABLED, true);
            searchIntent = extras.getParcelable(Intents.EXTRA_SEARCH);
            geocodeIntent = extras.getString(Intents.EXTRA_GEOCODE);
            coordsIntent = extras.getParcelable(Intents.EXTRA_COORDS);
            waypointTypeIntent = WaypointType.findById(extras.getString(Intents.EXTRA_WPTTYPE));
            mapStateIntent = extras.getIntArray(Intents.EXTRA_MAPSTATE);
            mapTitle = extras.getString(Intents.EXTRA_TITLE);
        } else {
            mapMode = MapMode.LIVE;
            isLiveEnabled = Settings.isLiveMap();
        }
        if (StringUtils.isBlank(mapTitle)) {
            mapTitle = res.getString(R.string.map_map);
        }

        Log.w("onCreate, extras=" + extras + ", savedInstanceState=" + savedInstanceState + ", isLiveEnabled=" + isLiveEnabled);

        ArrayList<Location> trailHistory = null;

        // Get fresh map information from the bundle if any
        if (savedInstanceState != null) {
            currentSourceId = savedInstanceState.getInt(BUNDLE_MAP_SOURCE, Settings.getMapSource().getNumericalId());
            mapStateIntent = savedInstanceState.getIntArray(BUNDLE_MAP_STATE);
            isLiveEnabled = savedInstanceState.getBoolean(BUNDLE_LIVE_ENABLED, false);
            trailHistory = savedInstanceState.getParcelableArrayList(BUNDLE_TRAIL_HISTORY);
        } else {
            currentSourceId = Settings.getMapSource().getNumericalId();
        }

        // If recreating from an obsolete map source, we may need a restart
//        if (changeMapSource(Settings.getMapSource())) {
//            return;
//        }

        // reset status
        noMapTokenShowed = false;

        ActivityMixin.onCreate(activity, true);

//        AMapOptions aOptions = new AMapOptions();
//        aOptions.


//        try {
//            MapsInitializer.initialize(activity.getApplicationContext());
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }

//        amapFragment = SupportMapFragment.newInstance();
//        activity.getSupportFragmentManager().beginTransaction().add(R.id.map_container, amapFragment).commit();

//        amapFragment = (SupportMapFragment)activity.getSupportFragmentManager().findFragmentById(R.id.map);


        mAmapMapView = (MapView) activity.findViewById(R.id.map);
        mAmapMapView.onCreate(savedInstanceState);// 此方法必须重写
        mAMap = mAmapMapView.getMap();
        mAMap.setLocationSource((LocationSource) activity);// 设置定位监听
        mAMap.getUiSettings().setMyLocationButtonEnabled(true);
        mAMap.setMyLocationEnabled(true);
        mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);



//

        // set layout
//        ActivityMixin.setTheme(activity);
//
//        if(activity instanceof AppCompatLayoutInflater){
//            View view = ((AppCompatLayoutInflater) activity).inflate(mapProvider.getMapLayoutId());
//            activity.setContentView(view);
//
//        }else{
//            activity.setContentView(mapProvider.getMapLayoutId());
//        }


        // set toolbar
//        setToolbar();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            if(isAppCompatActivity()){
//                ((AppCompatActivity)activity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            }else{
//                getActionBar().setDisplayHomeAsUpEnabled(true);
//            }
//        }

//        setTitle();



        // initialize map
//        mapView = (MapViewImpl) activity.findViewById(mapProvider.getMapViewId());
//        mapView.setMapSource();
//        mapView.setBuiltInZoomControls(true);
//        mapView.displayZoomControls(true);
//        mapView.preLoad();
//        mapView.setOnDragListener(new MapDragListener(this));

        // initialize overlays
//        mapView.clearOverlays();

//        overlayCaches = mapView.createAddMapOverlay(mapView.getContext(), Compatibility.getDrawable(getResources(), R.drawable.marker));
//
//
//        overlayPositionAndScale = mapView.createAddPositionAndScaleOverlay(coordsIntent, geocodeIntent);
//        if (trailHistory != null) {
//            overlayPositionAndScale.setHistory(trailHistory);
//        }


//        mapView.repaintRequired(null);
//
//        setZoom(Settings.getMapZoom(mapMode));
//        mapView.getMapController().setCenter(Settings.getMapCenter());
//
//        if (mapStateIntent == null) {
//            followMyLocation = followMyLocation && (mapMode == MapMode.LIVE);
//        } else {
//            followMyLocation = mapStateIntent[3] == 1;
//            if ((overlayCaches.getCircles() ? 1 : 0) != mapStateIntent[4]) {
//                overlayCaches.switchCircles();
//            }
//        }
//        if (geocodeIntent != null || searchIntent != null || coordsIntent != null || mapStateIntent != null) {
//            centerMap(geocodeIntent, searchIntent, coordsIntent, mapStateIntent);
//        }


//        final CheckBox locSwitch = ButterKnife.findById(activity, R.id.my_position);
//        if (locSwitch != null) {
//            initMyLocationSwitchButton(locSwitch);
//        }
//        prepareFilterBar();
//
//
//        LiveMapHint.getInstance().showHint(activity);
//        AndroidBeam.disable(activity);
    }


    boolean first = true;
    double mLat = 0;
    double mLon = 0;


    @Override
    public void onLocationChanged(double lat, double lon) {
        if(first){
            first = false;
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(lat,lon), 14, 0, 0));

            mLat = lat;
            mLon = lon;

            Log.w("before animation, latspan=" + getLatitudeSpan() + ", lonspan=" + getLongitudeSpan()
                    + ", visibleregion=" + getVisibleRegion());
            mAMap.animateCamera(update, 1000, mMapLoadedCallBack);



        }

    }

    AMap.CancelableCallback mMapLoadedCallBack = new AMap.CancelableCallback(){

        @Override
        public void onFinish() {

            Log.w("after animation, latspan=" + getLatitudeSpan() + ", lonspan=" + getLongitudeSpan()
                    + ", visibleregion=" + getVisibleRegion());


            mMainThreadHandler.sendEmptyMessage(MAIN_THREAD_LOAD);
//            mMainThreadHandler.sendEmptyMessage(MAIN_THREAD_TIMER);
        }

        @Override
        public void onCancel() {

        }
    };

    private void setToolbar(){
//        if (activity instanceof AppCompatActivity){
//            Toolbar toolbar = ButterKnife.findById(activity, R.id.toolbar);
//
//            Log.e("toolbar=" + toolbar + ", activity=" + activity);
//            ((AppCompatActivity) activity).setSupportActionBar(toolbar);
//        }

    }

    private void initMyLocationSwitchButton(final CheckBox locSwitch) {
        myLocSwitch = locSwitch;
        /* TODO: Switch back to ImageSwitcher for animations?
        myLocSwitch.setFactory(this);
        myLocSwitch.setInAnimation(activity, android.R.anim.fade_in);
        myLocSwitch.setOutAnimation(activity, android.R.anim.fade_out); */
        myLocSwitch.setOnClickListener(new MyLocationListener(this));
        switchMyLocationButton();
    }

    /**
     * Set the zoom of the map. The zoom is restricted to a certain minimum in case of live map.
     *
     */
    private void setZoom(final int zoom) {
        mapView.getMapController().setZoom(isLiveEnabled ? Math.max(zoom, MIN_LIVEMAP_ZOOM) : zoom);
    }

    private void prepareFilterBar() {
        // show the filter warning bar if the filter is set
        if (Settings.getCacheType() != CacheType.ALL) {
            final String cacheType = Settings.getCacheType().getL10n();
            final TextView filterTitleView = ButterKnife.findById(activity, R.id.filter_text);
            filterTitleView.setText(cacheType);
            activity.findViewById(R.id.filter_bar).setVisibility(View.VISIBLE);
        } else {
            activity.findViewById(R.id.filter_bar).setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();


        mAmapMapView.onResume();
//        mAMap = amapFragment.getMap();
//        mAMap.getUiSettings().setMyLocationButtonEnabled(true);
//        mAMap.setMyLocationEnabled(true);
//        mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

//        resumeSubscription = Subscriptions.from(geoDirUpdate.start(GeoDirHandler.UPDATE_GEODIR), startTimer());

        final List<String> toRefresh;
        synchronized (dirtyCaches) {
            toRefresh = new ArrayList<>(dirtyCaches);
            dirtyCaches.clear();
        }

        if (!toRefresh.isEmpty()) {
            AndroidRxUtils.refreshScheduler.createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    for (final String geocode: toRefresh) {
                        final Geocache cache = DataStore.loadCache(geocode, LoadFlags.LOAD_WAYPOINTS);
                        if (cache != null) {
                            // new collection type needs to remove first
                            caches.remove(cache);
                            // re-add to update the freshness
                            caches.add(cache);
                        }
                    }
                    displayExecutor.execute(new DisplayRunnable(CGeoMapAppCompat.this));
                }
            });
        }
    }

    @Override
    public void onPause() {
//        resumeSubscription.unsubscribe();
//        savePrefs();
//
//        mapView.destroyDrawingCache();
//
//        MapUtils.clearCachedItems();

        super.onPause();

        mAmapMapView.onPause();
        ((LocationSource)activity).deactivate();
    }

    @Override
    public void onStop() {
        // Ensure that handlers will not try to update the dialog once the view is detached.
        waitDialog = null;
        super.onStop();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        mAmapMapView.onDestroy();
    }

    private void menuCompass() {
        final Geocache cache = getSingleModeCache();
        if (cache != null) {
            CompassActivity.startActivityCache(this.getActivity(), cache);
        }
    }

    private void menuShowHint() {
        final Geocache cache = getSingleModeCache();
        if (cache != null) {
            cache.showHintToast(getActivity());
        }
    }

    private void selectMapTheme() {

        final File[] themeFiles = Settings.getMapThemeFiles();

        String currentTheme = StringUtils.EMPTY;
        final String currentThemePath = Settings.getCustomRenderThemeFilePath();
        if (StringUtils.isNotEmpty(currentThemePath)) {
            final File currentThemeFile = new File(currentThemePath);
            currentTheme = currentThemeFile.getName();
        }

        final List<String> names = new ArrayList<>();
        names.add(res.getString(R.string.map_theme_builtin));
        int currentItem = 0;
        for (final File file : themeFiles) {
            if (currentTheme.equalsIgnoreCase(file.getName())) {
                currentItem = names.size();
            }
            names.add(file.getName());
        }

        final int selectedItem = currentItem;

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(R.string.map_theme_select);

        builder.setSingleChoiceItems(names.toArray(new String[names.size()]), selectedItem,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int newItem) {
                        if (newItem != selectedItem) {
                            // Adjust index because of <default> selection
                            if (newItem > 0) {
                                Settings.setCustomRenderThemeFile(themeFiles[newItem - 1].getPath());
                            } else {
                                Settings.setCustomRenderThemeFile(StringUtils.EMPTY);
                            }
                            mapView.setMapTheme();
                        }
                        dialog.cancel();
                    }
                });

        builder.show();
    }

    /**
     * @return a non-null Set of geocodes corresponding to the caches that are shown on screen.
     */
    private Set<String> getGeocodesForCachesInViewport() {
        final Set<String> geocodes = new HashSet<>();
        final List<Geocache> cachesProtected = caches.getAsList();

        final Viewport viewport = mapView.getViewport();

        for (final Geocache cache : cachesProtected) {
            if (viewport.contains(cache)) {
                geocodes.add(cache.getGeocode());
            }
        }
        return geocodes;
    }

    /**
     * Restart the current activity if the map provider has changed, or change the map source if needed.
     *
     * @param newSource
     *            the new map source, which can be the same as the current one
     * @return true if a restart is needed, false otherwise
     */
    private boolean changeMapSource(@NonNull final MapSource newSource) {
        final MapSource oldSource = MapProviderFactory.getMapSource(currentSourceId);
        final boolean restartRequired = oldSource == null || !MapProviderFactory.isSameActivity(oldSource, newSource);

        Settings.setMapSource(newSource);
        currentSourceId = newSource.getNumericalId();

        if (restartRequired) {
            mapRestart();
        } else if (mapView != null) {  // changeMapSource can be called by onCreate()
            mapStateIntent = currentMapState();
            mapView.setMapSource();
            // re-center the map
            centered = false;
            centerMap(geocodeIntent, searchIntent, coordsIntent, mapStateIntent);
            // re-build menues
            ActivityMixin.invalidateOptionsMenu(activity);
        }

        return restartRequired;
    }

    /**
     * Restart the current activity with the default map source.
     */
    private void mapRestart() {
        // prepare information to restart a similar view
        final Intent mapIntent = new Intent(activity, Settings.getMapProvider().getMapClass());

        mapIntent.putExtra(Intents.EXTRA_SEARCH, searchIntent);
        mapIntent.putExtra(Intents.EXTRA_GEOCODE, geocodeIntent);
        if (coordsIntent != null) {
            mapIntent.putExtra(Intents.EXTRA_COORDS, coordsIntent);
        }
        mapIntent.putExtra(Intents.EXTRA_WPTTYPE, waypointTypeIntent != null ? waypointTypeIntent.id : null);
        mapIntent.putExtra(Intents.EXTRA_TITLE, mapTitle);
        mapIntent.putExtra(Intents.EXTRA_MAP_MODE, mapMode);
        mapIntent.putExtra(Intents.EXTRA_LIVE_ENABLED, isLiveEnabled);

        final int[] mapState = currentMapState();
        if (mapState != null) {
            mapIntent.putExtra(Intents.EXTRA_MAPSTATE, mapState);
        }

        // close old map
        activity.finish();

        // start the new map
        activity.startActivity(mapIntent);
    }

    /**
     * Get the current map state from the map view if it exists or from the mapStateIntent field otherwise.
     *
     * @return the current map state as an array of int, or null if no map state is available
     */
    private int[] currentMapState() {
        if (mapView == null) {
            return null;
        }
        final GeoPointImpl mapCenter = mapView.getMapViewCenter();
        return new int[] {
                mapCenter.getLatitudeE6(),
                mapCenter.getLongitudeE6(),
                mapView.getMapZoomLevel(),
                followMyLocation ? 1 : 0,
                overlayCaches.getCircles() ? 1 : 0
        };
    }

    private void savePrefs() {
        Settings.setMapZoom(mapMode, mapView.getMapZoomLevel());
        Settings.setMapCenter(mapView.getMapViewCenter());
    }

    // Set center of map to my location if appropriate.
    private void myLocationInMiddle(final GeoData geo) {
        if (followMyLocation) {
            centerMap(geo.getCoords());
        }
    }

    // class: update location
    private static class UpdateLoc extends GeoDirHandler {
        // use the following constants for fine tuning - find good compromise between smooth updates and as less updates as possible

        // minimum time in milliseconds between position overlay updates
        private static final long MIN_UPDATE_INTERVAL = 500;
        // minimum change of heading in grad for position overlay update
        private static final float MIN_HEADING_DELTA = 15f;
        // minimum change of location in fraction of map width/height (whatever is smaller) for position overlay update
        private static final float MIN_LOCATION_DELTA = 0.01f;

        Location currentLocation = Sensors.getInstance().currentGeo();
        float currentHeading;

        private long timeLastPositionOverlayCalculation = 0;
        /**
         * weak reference to the outer class
         */
        private final WeakReference<CGeoMapAppCompat> mapRef;

        UpdateLoc(final CGeoMapAppCompat map) {
            mapRef = new WeakReference<>(map);
        }

        @Override
        public void updateGeoDir(@NonNull final GeoData geo, final float dir) {
            currentLocation = geo;
            currentHeading = AngleUtils.getDirectionNow(dir);
            repaintPositionOverlay();
        }

        /**
         * Repaint position overlay but only with a max frequency and if position or heading changes sufficiently.
         */
        void repaintPositionOverlay() {
            final long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis > timeLastPositionOverlayCalculation + MIN_UPDATE_INTERVAL) {
                timeLastPositionOverlayCalculation = currentTimeMillis;

                try {
                    final CGeoMapAppCompat map = mapRef.get();
                    if (map != null) {
                        final boolean needsRepaintForDistanceOrAccuracy = needsRepaintForDistanceOrAccuracy();
                        final boolean needsRepaintForHeading = needsRepaintForHeading();

                        if (needsRepaintForDistanceOrAccuracy && followMyLocation) {
                            map.centerMap(new Geopoint(currentLocation));
                        }

                        if (needsRepaintForDistanceOrAccuracy || needsRepaintForHeading) {

                            map.overlayPositionAndScale.setCoordinates(currentLocation);
                            map.overlayPositionAndScale.setHeading(currentHeading);
                            map.mapView.repaintRequired(map.overlayPositionAndScale);
                        }
                    }
                } catch (final RuntimeException e) {
                    Log.w("Failed to update location", e);
                }
            }
        }

        boolean needsRepaintForHeading() {
            final CGeoMapAppCompat map = mapRef.get();
            if (map == null) {
                return false;
            }
            return Math.abs(AngleUtils.difference(currentHeading, map.overlayPositionAndScale.getHeading())) > MIN_HEADING_DELTA;
        }

        boolean needsRepaintForDistanceOrAccuracy() {
            final CGeoMapAppCompat map = mapRef.get();
            if (map == null) {
                return false;
            }
            final Location lastLocation = map.overlayPositionAndScale.getCoordinates();

            float dist = Float.MAX_VALUE;
            if (lastLocation != null) {
                if (lastLocation.getAccuracy() != currentLocation.getAccuracy()) {
                    return true;
                }
                dist = currentLocation.distanceTo(lastLocation);
            }

            final float[] mapDimension = new float[1];
            if (map.mapView.getWidth() < map.mapView.getHeight()) {
                final double span = map.mapView.getLongitudeSpan() / 1e6;
                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), currentLocation.getLatitude(), currentLocation.getLongitude() + span, mapDimension);
            } else {
                final double span = map.mapView.getLatitudeSpan() / 1e6;
                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), currentLocation.getLatitude() + span, currentLocation.getLongitude(), mapDimension);
            }

            return dist > (mapDimension[0] * MIN_LOCATION_DELTA);
        }
    }

    /**
     * Starts the load timer.
     */

    private Subscription startTimer() {
        if (coordsIntent != null) {
            // display just one point
            displayPoint(coordsIntent);
            loadTimer = Subscriptions.empty();
        } else {
            loadTimer = Schedulers.newThread().createWorker().schedulePeriodically(new LoadTimerAction(this), 0, 250, TimeUnit.MILLISECONDS);
        }
        return loadTimer;
    }

    private static final class LoadTimerAction implements Action0 {

        @NonNull private final WeakReference<CGeoMapAppCompat> mapRef;
        private int previousZoom = -100;
        private Viewport previousViewport;

        LoadTimerAction(@NonNull final CGeoMapAppCompat map) {
            this.mapRef = new WeakReference<>(map);
        }

        @Override
        public void call() {
            final CGeoMapAppCompat map = mapRef.get();
            if (map == null) {
                return;
            }
            try {
                // get current viewport
                final Viewport viewportNow = map.mapView.getViewport();
                // Since zoomNow is used only for local comparison purposes,
                // it is ok to use the Google Maps compatible zoom level of OSM Maps
                final int zoomNow = map.mapView.getMapZoomLevel();

                // check if map moved or zoomed
                //TODO Portree Use Rectangle inside with bigger search window. That will stop reloading on every move
                final boolean moved = map.markersInvalidated || (map.isLiveEnabled && !map.downloaded) || (previousViewport == null) || zoomNow != previousZoom ||
                        (mapMoved(previousViewport, viewportNow) && (map.cachesCnt <= 0 || CollectionUtils.isEmpty(map.caches) || !previousViewport.includes(viewportNow)));

                // update title on any change
                if (moved || !viewportNow.equals(previousViewport)) {
                    map.updateMapTitle();
                }

                // save new values
                if (moved) {
                    map.markersInvalidated = false;

                    final long currentTime = System.currentTimeMillis();

                    if ((currentTime - map.loadThreadRun) > 1000) {
                        previousViewport = viewportNow;
                        previousZoom = zoomNow;
                        loadExecutor.execute(new LoadRunnable(map));
                    }
                }
            } catch (final Exception e) {
                Log.w("CGeoMap.startLoadtimer.start", e);
            }
        }
    }

    /**
     * get if map is loading something
     *
     */
    private boolean isLoading() {
        return !loadTimer.isUnsubscribed() &&
                (loadExecutor.getActiveCount() > 0 ||
                        downloadExecutor.getActiveCount() > 0 ||
                        displayExecutor.getActiveCount() > 0);
    }

    /**
     * Worker thread that loads caches and waypoints from the database and then spawns the {@link DownloadRunnable}.
     * started by the load timer.
     */

    private static class LoadRunnable extends DoRunnable {

        LoadRunnable(@NonNull final CGeoMapAppCompat map) {
            super(map);
        }

        @Override
        public void runWithMap(final CGeoMapAppCompat map) {
            map.doLoadRun();
        }
    }

    public VisibleRegion getVisibleRegion(){
        final Projection projection = mAMap.getProjection();
        if (projection != null && mAmapMapView.getHeight() > 0) {
            return projection.getVisibleRegion();
        }
        return null;
    }

    public int getLatitudeSpan() {
        int span = 0;
        final Projection projection = mAMap.getProjection();
        if (projection != null && mAmapMapView.getHeight() > 0) {
            LatLng lt = projection.fromScreenLocation(new Point(0,0));
            LatLng rb = projection.fromScreenLocation(new Point(mAmapMapView.getWidth(),mAmapMapView.getHeight()));

            return (int)(Math.abs(rb.latitude*1e6 - lt.latitude*1e6));
        }

        return 0;
    }

    public int getLongitudeSpan() {
        int span = 0;
        final Projection projection = mAMap.getProjection();
        if (projection != null && mAmapMapView.getHeight() > 0) {
            LatLng lt = projection.fromScreenLocation(new Point(0,0));
            LatLng rb = projection.fromScreenLocation(new Point(mAmapMapView.getWidth(),mAmapMapView.getHeight()));

            return (int)(Math.abs(rb.longitude*1e6 - lt.longitude*1e6));
        }

        return 0;
    }

    public Viewport getCurrViewport(){

        Log.w("getCurrViewport, latspan=" + getLatitudeSpan() + ", lonspan=" + getLongitudeSpan()
                + ", visibleregion=" + getVisibleRegion());
        return new Viewport(new AMapGeoPoint(mLat, mLon), getLatitudeSpan() / 1e6, getLongitudeSpan() / 1e6);
    }

    Viewport mViewport;

    private void doLoadRun() {
        try {
//            showProgressHandler.sendEmptyMessage(SHOW_PROGRESS);
            loadThreadRun = System.currentTimeMillis();


//            mViewport = new Viewport(new AMapGeoPoint(mLat, mLon), getLatitudeSpan() / 1e6, getLongitudeSpan() / 1e6);
            mViewport = getCurrViewport();

            Log.w("doLoadRun, " + ", isLiveEnabled=" + isLiveEnabled + ", mViewport=" + mViewport);

            final SearchResult searchResult;
            if (mapMode == MapMode.LIVE) {
                searchResult = isLiveEnabled ? new SearchResult() : new SearchResult(DataStore.loadStoredInViewport(mViewport, Settings.getCacheType()));
            } else {
                // map started from another activity
                searchResult = searchIntent != null ? new SearchResult(searchIntent) : new SearchResult();
                if (geocodeIntent != null) {
                    searchResult.addGeocode(geocodeIntent);
                }
            }
            // live mode search result
            if (isLiveEnabled) {
                searchResult.addSearchResult(DataStore.loadCachedInViewport(mViewport, Settings.getCacheType()));
            }

            downloaded = true;
            final Set<Geocache> cachesFromSearchResult = searchResult.getCachesFromSearchResult(LoadFlags.LOAD_WAYPOINTS);
            // update the caches
            // new collection type needs to remove first
            caches.removeAll(cachesFromSearchResult);
            caches.addAll(cachesFromSearchResult);

            final boolean excludeMine = Settings.isExcludeMyCaches();
            final boolean excludeDisabled = Settings.isExcludeDisabledCaches();
            if (mapMode == MapMode.LIVE) {
                synchronized (caches) {
                    filter(caches);
                }
            }
            countVisibleCaches();
//            if (cachesCnt < Settings.getWayPointsThreshold() || geocodeIntent != null) {
//                // we don't want to see any stale waypoints
//                waypoints.clear();
//                if (isLiveEnabled || mapMode == MapMode.LIVE
//                        || mapMode == MapMode.COORDS) {
//                    //All visible waypoints
//                    final CacheType type = Settings.getCacheType();
//                    final Set<Waypoint> waypointsInViewport = DataStore.loadWaypoints(mViewport, excludeMine, excludeDisabled, type);
//                    waypoints.addAll(waypointsInViewport);
//                } else {
//                    //All waypoints from the viewed caches
//                    for (final Geocache c : caches.getAsList()) {
//                        waypoints.addAll(c.getWaypoints());
//                    }
//                }
//            } else {
//                // we don't want to see any stale waypoints when above threshold
//                waypoints.clear();
//            }

            //render
            displayExecutor.execute(new DisplayRunnable(this));

            if (isLiveEnabled) {
                downloadExecutor.execute(new DownloadRunnable(this));
            }
            lastSearchResult = searchResult;
        } finally {
            showProgressHandler.sendEmptyMessage(HIDE_PROGRESS); // hide progress
        }

    }

    /**
     * Worker thread downloading caches from the internet.
     * Started by {@link LoadRunnable}.
     */

    private static class DownloadRunnable extends DoRunnable {

        DownloadRunnable(final CGeoMapAppCompat map) {
            super(map);
        }

        @Override
        public void runWithMap(final CGeoMapAppCompat map) {
            map.doDownloadRun();
        }
    }

    private void doDownloadRun() {
        try {
//            showProgressHandler.sendEmptyMessage(SHOW_PROGRESS); // show progress
            if (Settings.isGCConnectorActive() && tokens == null) {
                tokens = GCLogin.getInstance().getMapTokens();
                if (StringUtils.isEmpty(tokens.getUserSession()) || StringUtils.isEmpty(tokens.getSessionToken())) {
                    tokens = null;
                    if (!noMapTokenShowed) {
                        ActivityMixin.showToast(activity, res.getString(R.string.map_token_err));
                        noMapTokenShowed = true;
                    }
                }
            }
//            final SearchResult searchResult = ConnectorFactory.searchByViewport(mViewport.resize(0.8), tokens);
            final SearchResult searchResult = ConnectorFactory.searchByViewport(getCurrViewport(), tokens);

            downloaded = true;

            final Set<Geocache> result = searchResult.getCachesFromSearchResult(LoadFlags.LOAD_CACHE_OR_DB);

            Log.w("doDownloadRun, " + ", result.size=" + result.size());

            filter(result);
            // update the caches
            // first remove filtered out
            final Set<String> filteredCodes = searchResult.getFilteredGeocodes();
            Log.d("Filtering out " + filteredCodes.size() + " caches: " + filteredCodes.toString());
            caches.removeAll(DataStore.loadCaches(filteredCodes, LoadFlags.LOAD_CACHE_ONLY));
            DataStore.removeCaches(filteredCodes, EnumSet.of(RemoveFlag.CACHE));
            // new collection type needs to remove first to refresh
            caches.removeAll(result);
            caches.addAll(result);
            lastSearchResult = searchResult;

            //render
            displayExecutor.execute(new DisplayRunnable(this));

        } finally {
//            showProgressHandler.sendEmptyMessage(HIDE_PROGRESS); // hide progress
        }
    }

    /**
     * Thread to Display (down)loaded caches. Started by {@link LoadRunnable} and {@link DownloadRunnable}
     */
    private static class DisplayRunnable extends DoRunnable {

        DisplayRunnable(@NonNull final CGeoMapAppCompat map) {
            super(map);
        }

        @Override
        public void runWithMap(final CGeoMapAppCompat map) {
            map.doDisplayRun();
        }
    }

    private void doDisplayRun() {
        try {
//            showProgressHandler.sendEmptyMessage(SHOW_PROGRESS);

            // display caches
            final List<Geocache> cachesToDisplay = caches.getAsList();
            final List<Waypoint> waypointsToDisplay = new ArrayList<>(waypoints);
            final List<CachesOverlayItemImpl> itemsToDisplay = new ArrayList<>();

//            if (!cachesToDisplay.isEmpty()) {
//                // Only show waypoints for single view or setting
//                // when less than showWaypointsthreshold Caches shown
//                if (mapMode == MapMode.SINGLE || (cachesCnt < Settings.getWayPointsThreshold())) {
//                    for (final Waypoint waypoint : waypointsToDisplay) {
//
//                        if (waypoint == null || waypoint.getCoords() == null) {
//                            continue;
//                        }
//
//                        itemsToDisplay.add(getWaypointItem(waypoint));
//                    }
//                }
//                for (final Geocache cache : cachesToDisplay) {
//
//                    if (cache == null || cache.getCoords() == null) {
//                        continue;
//                    }
//                    itemsToDisplay.add(getCacheItem(cache));
//                }
//            }
            // don't add other waypoints to overlayCaches if just one point should be displayed
//            if (coordsIntent == null) {
//                overlayCaches.updateItems(itemsToDisplay);
//            }
//            displayHandler.sendEmptyMessage(INVALIDATE_MAP);

//            updateMapTitle();

            addCacheToMap(cachesToDisplay);


        } finally {
//            showProgressHandler.sendEmptyMessage(HIDE_PROGRESS);
        }
    }

    private void addCacheToMap(List<Geocache> cachesToDisplay){


//        TextOptions textOptions = new TextOptions()
//                .position(new LatLng(mLat, mLon))
//                .text("Text")
//                .fontColor(Color.BLACK)
//                .backgroundColor(Color.BLUE)
//                .fontSize(30)
//                .rotate(20)
//                .align(Text.ALIGN_CENTER_HORIZONTAL, Text.ALIGN_CENTER_VERTICAL)
//                .zIndex(1.f).typeface(Typeface.DEFAULT_BOLD);
////        mAMap.addText(textOptions);
//        Message msg = Message.obtain();
//        msg.obj = textOptions;
//        mMainThreadHandler.sendMessage(msg);

        Log.w("cachesToDisplay.size=" + cachesToDisplay.size() + ", mLat=" + mLat + ", mLon=" + mLon);

        BitmapDescriptor iconBd = BitmapDescriptorFactory.fromResource(R.drawable.mappin_traditional_collapsed);


        ArrayList<MarkerOptions> markerOptionsesToDisplay = new ArrayList<>();
        double[] correctLatLon = new double[2];

//        JZLocationConverter.LatLng correctLatLon = null;
        for (Geocache cache : cachesToDisplay) {



//            GpsCorrector.transform(cache.getCoords().getLatitude(), cache.getCoords().getLongitude(), correctLatLon);
            correctLatLon = TransformUtil.transform(cache.getCoords().getLatitude(), cache.getCoords().getLongitude());

            Log.w("cache：" +  "lat=" + cache.getCoords().getLatitude() + ", lon=" + cache.getCoords().getLongitude()
                + ", clat=" + correctLatLon[0] + ", clon=" + correctLatLon[1]);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(iconBd)
            .position(new LatLng(correctLatLon[0], correctLatLon[1]));


//            correctLatLon = JZLocationConverter.gcj02Decrypt(cache.getCoords().getLatitude(), cache.getCoords().getLongitude());
//            Log.w("cache：" +  "lat=" + cache.getCoords().getLatitude() + ", lon=" + cache.getCoords().getLongitude()
//                + ", clat=" + correctLatLon.latitude + ", clon=" + correctLatLon.longitude);
//
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.icon(iconBd)
//                .position(new LatLng(correctLatLon.latitude, correctLatLon.longitude));

            markerOptionsesToDisplay.add(markerOptions);

        }

        Message markersMsg = Message.obtain();
        markersMsg.what = MAIN_THREAD_ADD_MARKERS;
        markersMsg.obj = markerOptionsesToDisplay;
        mMainThreadHandler.sendMessage(markersMsg);

    }


    private void displayPoint(final Geopoint coords) {
        final Waypoint waypoint = new Waypoint("some place", waypointTypeIntent != null ? waypointTypeIntent : WaypointType.WAYPOINT, false);
        waypoint.setCoords(coords);

        final CachesOverlayItemImpl item = getWaypointItem(waypoint);
        overlayCaches.updateItems(item);
        displayHandler.sendEmptyMessage(INVALIDATE_MAP);
        updateMapTitle();

        cachesCnt = 1;
    }

    private void updateMapTitle() {
        displayHandler.sendEmptyMessage(UPDATE_TITLE);
    }

    private abstract static class DoRunnable implements Runnable {

        private final WeakReference<CGeoMapAppCompat> mapRef;

        protected DoRunnable(@NonNull final CGeoMapAppCompat map) {
            mapRef = new WeakReference<>(map);
        }

        @Override
        public final void run() {
            final CGeoMapAppCompat map = mapRef.get();
            if (map != null) {
                runWithMap(map);
            }
        }

        protected abstract void runWithMap(final CGeoMapAppCompat map);
    }

    /**
     * store caches, invoked by "store offline" menu item
     *
     * @param listId
     *            the list to store the caches in
     */
    private void storeCaches(final List<String> geocodes, final int listId) {
        final LoadDetailsHandler loadDetailsHandler = new LoadDetailsHandler();

        waitDialog = new ProgressDialog(activity);
        waitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        waitDialog.setCancelable(true);
        waitDialog.setCancelMessage(loadDetailsHandler.cancelMessage());
        waitDialog.setMax(detailTotal);
        waitDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(final DialogInterface arg0) {
                try {
                    if (loadDetailsThread != null) {
                        loadDetailsThread.stopIt();
                    }
                } catch (final Exception e) {
                    Log.e("CGeoMap.storeCaches.onCancel", e);
                }
            }
        });

        final float etaTime = detailTotal * 7.0f / 60.0f;
        final int roundedEta = Math.round(etaTime);
        if (etaTime < 0.4) {
            waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + res.getString(R.string.caches_eta_ltm));
        } else {
            waitDialog.setMessage(res.getString(R.string.caches_downloading) + " " + res.getQuantityString(R.plurals.caches_eta_mins, roundedEta, roundedEta));
        }
        waitDialog.show();

        detailProgressTime = System.currentTimeMillis();

        loadDetailsThread = new LoadDetails(loadDetailsHandler, geocodes, listId);
        loadDetailsThread.start();
    }

    /**
     * Thread to store the caches in the viewport. Started by Activity.
     */

    private class LoadDetails extends Thread {

        private final CancellableHandler handler;
        private final List<String> geocodes;
        private final int listId;

        LoadDetails(final CancellableHandler handler, final List<String> geocodes, final int listId) {
            this.handler = handler;
            this.geocodes = geocodes;
            this.listId = listId;
        }

        public void stopIt() {
            handler.cancel();
        }

        @Override
        public void run() {
            if (CollectionUtils.isEmpty(geocodes)) {
                return;
            }

            for (final String geocode : geocodes) {
                try {
                    if (handler.isCancelled()) {
                        break;
                    }

                    if (!DataStore.isOffline(geocode, null)) {
                        final Set<Integer> lists = new HashSet<>();
                        lists.add(listId);
                        Geocache.storeCache(null, geocode, lists, false, handler);
                    }
                } catch (final Exception e) {
                    Log.e("CGeoMap.LoadDetails.run", e);
                } finally {
                    // one more cache over
                    detailProgress++;
                    handler.sendEmptyMessage(UPDATE_PROGRESS);
                }
            }

            // we're done
            handler.sendEmptyMessage(FINISHED_LOADING_DETAILS);
        }
    }

    private static synchronized void filter(final Collection<Geocache> caches) {
        final boolean excludeMine = Settings.isExcludeMyCaches();
        final boolean excludeDisabled = Settings.isExcludeDisabledCaches();

        final List<Geocache> removeList = new ArrayList<>();
        for (final Geocache cache : caches) {
            if ((excludeMine && cache.isFound()) || (excludeMine && cache.isOwner()) || (excludeDisabled && cache.isDisabled()) || (excludeDisabled && cache.isArchived())) {
                removeList.add(cache);
            }
        }
        caches.removeAll(removeList);
    }

    private static boolean mapMoved(final Viewport referenceViewport, final Viewport newViewport) {
        return Math.abs(newViewport.getLatitudeSpan() - referenceViewport.getLatitudeSpan()) > 50e-6 ||
                Math.abs(newViewport.getLongitudeSpan() - referenceViewport.getLongitudeSpan()) > 50e-6 ||
                Math.abs(newViewport.center.getLatitude() - referenceViewport.center.getLatitude()) > referenceViewport.getLatitudeSpan() / 4 ||
                Math.abs(newViewport.center.getLongitude() - referenceViewport.center.getLongitude()) > referenceViewport.getLongitudeSpan() / 4;
    }

    // center map to desired location
    private void centerMap(final Geopoint coords) {
        if (coords == null) {
            return;
        }

        final MapControllerImpl mapController = mapView.getMapController();
        final GeoPointImpl target = makeGeoPoint(coords);

        if (alreadyCentered) {
            mapController.animateTo(target);
        } else {
            mapController.setCenter(target);
        }

        alreadyCentered = true;

    }

    // move map to view results of searchIntent
    private void centerMap(final String geocodeCenter, final SearchResult searchCenter, final Geopoint coordsCenter, final int[] mapState) {
        final MapControllerImpl mapController = mapView.getMapController();

        if (!centered && mapState != null) {
            try {
                mapController.setCenter(mapItemFactory.getGeoPointBase(new Geopoint(mapState[0] / 1.0e6, mapState[1] / 1.0e6)));
                setZoom(mapState[2]);
            } catch (final RuntimeException e) {
                Log.e("centermap", e);
            }

            centered = true;
            alreadyCentered = true;
        } else if (!centered && (geocodeCenter != null || searchIntent != null)) {
            try {
                Viewport viewport = null;

                if (geocodeCenter != null) {
                    viewport = DataStore.getBounds(geocodeCenter);
                } else if (searchCenter != null) {
                    viewport = DataStore.getBounds(searchCenter.getGeocodes());
                }

                if (viewport == null) {
                    return;
                }

                mapController.setCenter(mapItemFactory.getGeoPointBase(viewport.center));
                if (viewport.getLatitudeSpan() != 0 && viewport.getLongitudeSpan() != 0) {
                    mapController.zoomToSpan((int) (viewport.getLatitudeSpan() * 1e6), (int) (viewport.getLongitudeSpan() * 1e6));
                }
            } catch (final RuntimeException e) {
                Log.e("centermap", e);
            }

            centered = true;
            alreadyCentered = true;
        } else if (!centered && coordsCenter != null) {
            try {
                mapController.setCenter(makeGeoPoint(coordsCenter));
            } catch (final Exception e) {
                Log.e("centermap", e);
            }

            centered = true;
            alreadyCentered = true;
        }
    }

    // switch My Location button image
    private void switchMyLocationButton() {
        // FIXME: temporary workaround for the absence of "follow my location" on Android 3.x (see issue #4289).
        if (myLocSwitch != null) {
            myLocSwitch.setChecked(followMyLocation);
            if (followMyLocation) {
                myLocationInMiddle(Sensors.getInstance().currentGeo());
            }
        }
    }

    // set my location listener
    private static class MyLocationListener implements View.OnClickListener {

        private final WeakReference<CGeoMapAppCompat> mapRef;

        MyLocationListener(@NonNull final CGeoMapAppCompat map) {
            mapRef = new WeakReference<>(map);
        }

        @Override
        public void onClick(final View view) {
            final CGeoMapAppCompat map = mapRef.get();
            if (map != null) {
                map.onFollowMyLocationClicked();
            }
        }
    }

    private void onFollowMyLocationClicked() {
        followMyLocation = !followMyLocation;
        switchMyLocationButton();
    }

    public static class MapDragListener implements OnMapDragListener {

        private final WeakReference<CGeoMapAppCompat> mapRef;

        public MapDragListener(@NonNull final CGeoMapAppCompat map) {
            mapRef = new WeakReference<>(map);
        }

        @Override
        public void onDrag() {
            final CGeoMapAppCompat map = mapRef.get();
            if (map != null) {
                map.onDrag();
            }
        }

    }

    private void onDrag() {
        if (followMyLocation) {
            followMyLocation = false;
            switchMyLocationButton();
        }
    }

    // make geopoint
    private GeoPointImpl makeGeoPoint(final Geopoint coords) {
        return mapItemFactory.getGeoPointBase(coords);
    }

    @Override
    public View makeView() {
        final ImageView imageView = new ImageView(activity);
        imageView.setScaleType(ScaleType.CENTER);
        imageView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return imageView;
    }

    private static Intent newIntent(final Context context) {
        return new Intent(context, Settings.getMapProvider().getMapClass());
    }

    public static void startActivitySearch(final Activity fromActivity, final SearchResult search, final String title) {
        final Intent mapIntent = newIntent(fromActivity);
        mapIntent.putExtra(Intents.EXTRA_SEARCH, search);
        mapIntent.putExtra(Intents.EXTRA_MAP_MODE, MapMode.LIST);
        mapIntent.putExtra(Intents.EXTRA_LIVE_ENABLED, false);
        if (StringUtils.isNotBlank(title)) {
            mapIntent.putExtra(Intents.EXTRA_TITLE, title);
        }
        fromActivity.startActivity(mapIntent);
    }

    public static Intent getLiveMapIntent(final Activity fromActivity) {
        return newIntent(fromActivity)
                .putExtra(Intents.EXTRA_MAP_MODE, MapMode.LIVE)
                .putExtra(Intents.EXTRA_LIVE_ENABLED, Settings.isLiveMap());
    }

    public static void startActivityCoords(final Activity fromActivity, final Geopoint coords, final WaypointType type, final String title) {
        final Intent mapIntent = newIntent(fromActivity);
        mapIntent.putExtra(Intents.EXTRA_MAP_MODE, MapMode.COORDS);
        mapIntent.putExtra(Intents.EXTRA_LIVE_ENABLED, false);
        mapIntent.putExtra(Intents.EXTRA_COORDS, coords);
        if (type != null) {
            mapIntent.putExtra(Intents.EXTRA_WPTTYPE, type.id);
        }
        if (StringUtils.isNotBlank(title)) {
            mapIntent.putExtra(Intents.EXTRA_TITLE, title);
        }
        fromActivity.startActivity(mapIntent);
    }

    public static void startActivityGeoCode(final Activity fromActivity, final String geocode) {
        final Intent mapIntent = newIntent(fromActivity);
        mapIntent.putExtra(Intents.EXTRA_MAP_MODE, MapMode.SINGLE);
        mapIntent.putExtra(Intents.EXTRA_LIVE_ENABLED, false);
        mapIntent.putExtra(Intents.EXTRA_GEOCODE, geocode);
        fromActivity.startActivity(mapIntent);
    }

    public static void markCacheAsDirty(final String geocode) {
        synchronized (dirtyCaches) {
            dirtyCaches.add(geocode);
        }
    }

    private CachesOverlayItemImpl getCacheItem(final Geocache cache) {
        final CachesOverlayItemImpl item = mapItemFactory.getCachesOverlayItem(cache, cache.applyDistanceRule());
        item.setMarker(MapUtils.getCacheMarker(getResources(), cache));
        return item;
    }

    private CachesOverlayItemImpl getWaypointItem(final Waypoint waypoint) {
        final CachesOverlayItemImpl item = mapItemFactory.getCachesOverlayItem(waypoint, waypoint.getWaypointType().applyDistanceRule());
        item.setMarker(MapUtils.getWaypointMarker(getResources(), waypoint));
        return item;
    }

}
