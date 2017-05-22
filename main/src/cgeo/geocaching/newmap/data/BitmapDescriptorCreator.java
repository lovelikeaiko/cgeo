package cgeo.geocaching.newmap.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by paint on 17-5-22.
 */

public class BitmapDescriptorCreator implements Parcelable.Creator<BitmapDescriptor> {
    public BitmapDescriptorCreator() {
    }

    public BitmapDescriptor createFromParcel(Parcel var1) {
        BitmapDescriptor var2 = new BitmapDescriptor((Bitmap)null);
        var2.c = (Bitmap)var1.readParcelable(BitmapDescriptor.class.getClassLoader());
        var2.a = var1.readInt();
        var2.b = var1.readInt();
        return var2;
    }

    public BitmapDescriptor[] newArray(int var1) {
        return new BitmapDescriptor[var1];
    }
}