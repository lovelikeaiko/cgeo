package cgeo.geocaching.maps.interfaces;

import android.app.Activity;

import cgeo.geocaching.newmap.fragment.FragmentFactory;
import cgeo.geocaching.newmap.interfaces.MapApiImpl;

/**
 * Defines functions of a factory class to get implementation specific objects
 * (GeoPoints, OverlayItems, ...)
 */
public interface MapProvider {

    boolean isSameActivity(final MapSource source1, final MapSource source2);

    Class<? extends Activity> getMapClass();

    int getMapViewId();

    /**
     * fragment的layout
     * @return
     */
    int getMapLayoutId();

    FragmentFactory getFragmentFactory();

    MapItemFactory getMapItemFactory();

    void registerMapSource(final MapSource mapSource);

    MapApiImpl createMapApi();
}
