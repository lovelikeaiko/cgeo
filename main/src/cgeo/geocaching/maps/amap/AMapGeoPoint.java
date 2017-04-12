package cgeo.geocaching.maps.amap;

import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.maps.interfaces.GeoPointImpl;

/**
 * Created by paint on 7/20/16.
 */
public class AMapGeoPoint implements GeoPointImpl{

    private static final double CONVERSION_FACTOR = 1000000.0D;

    private final double latitude;
    private final double longitude;


    public AMapGeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public int getLatitudeE6() {
        return (int)(latitude*CONVERSION_FACTOR);
    }

    @Override
    public int getLongitudeE6() {
        return (int)(longitude*CONVERSION_FACTOR);
    }

    @Override
    public Geopoint getCoords() {
        return new Geopoint(latitude, longitude);
    }
}
