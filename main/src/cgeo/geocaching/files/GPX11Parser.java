package cgeo.geocaching.files;

import android.sax.Element;

public final class GPX11Parser extends GPXParser {

    public GPX11Parser(final int listIdIn) {
        super(listIdIn, "http://www.topografix.com/GPX/1/1", "1.1");
    }

    @Override
    protected Element getCacheParent(final Element waypoint) {
        return waypoint.getChild(namespace, "extensions");
    }

}
