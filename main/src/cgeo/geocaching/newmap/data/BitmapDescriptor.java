package cgeo.geocaching.newmap.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by paint on 17-5-22.
 */

public class BitmapDescriptor implements Parcelable, Cloneable {
    public static final BitmapDescriptorCreator CREATOR = new BitmapDescriptorCreator();
    int width = 0;
    int height = 0;
    Bitmap bitmap;
    int hashCode = 0;

    BitmapDescriptor(Bitmap bitmap, int hashCode) {
        if(bitmap != null) {
            this.width = bitmap.getWidth();
            this.height = bitmap.getHeight();
            this.hashCode = hashCode;

            try {
                this.bitmap = bitmap.copy(bitmap.getConfig(), false);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }

    private BitmapDescriptor(Bitmap bitmap, int width, int height, int hashCode) {
        this.width = width;
        this.height = height;
        this.bitmap = bitmap;
        this.hashCode = hashCode;
    }

    public BitmapDescriptor clone() {
        try {
            BitmapDescriptor var1 = new BitmapDescriptor(Bitmap.createBitmap(this.bitmap), this.width, this.height, this.hashCode);
            return var1;
        } catch (Throwable var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getHashCode() {
        return this.hashCode;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel var1, int var2) {
        var1.writeParcelable(this.bitmap, var2);
        var1.writeInt(this.width);
        var1.writeInt(this.height);
        var1.writeInt(this.hashCode);
    }

    public void recycle() {
        if(this.bitmap != null && !this.bitmap.isRecycled()) {
            this.bitmap.recycle();
            this.bitmap = null;
        }

    }

    public boolean equals(Object var1) {
        if(this.bitmap != null && !this.bitmap.isRecycled()) {
            if(var1 == null) {
                return false;
            } else if(this == var1) {
                return true;
            } else if(this.getClass() != var1.getClass()) {
                return false;
            } else {
                BitmapDescriptor var2 = (BitmapDescriptor)var1;
                if(var2.bitmap != null && !var2.bitmap.isRecycled()) {
                    if(this.width == var2.getWidth() && this.height == var2.getHeight()) {
                        try {
//                            return this.bitmap.sameAs(var2.bitmap);
                            return this.hashCode==var2.hashCode;
                        } catch (Throwable var4) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        return hashCode;
    }
}