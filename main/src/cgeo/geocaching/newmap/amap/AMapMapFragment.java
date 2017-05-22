package cgeo.geocaching.newmap.amap;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cgeo.geocaching.R;

/**
 * Created by paint on 17-5-22.
 */

public class AMapMapFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.amap_basemap_support_fragment, container);
        return view;
    }
}
