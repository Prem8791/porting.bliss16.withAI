package android.app.prodx;

import android.os.Parcel;
import android.os.Parcelable;

public class ProdXCapabilityRequest implements Parcelable {
    private final ProdXCapabilityDescriptor mDescriptor;
    private final byte[] mParameters;

    public ProdXCapabilityRequest(ProdXCapabilityDescriptor descriptor, byte[] parameters) {
        mDescriptor = descriptor;
        mParameters = parameters;
    }

    protected ProdXCapabilityRequest(Parcel in) {
        mDescriptor = in.readParcelable(ProdXCapabilityDescriptor.class.getClassLoader(), ProdXCapabilityDescriptor.class);
        mParameters = in.createByteArray();
    }

    public ProdXCapabilityDescriptor getDescriptor() { return mDescriptor; }
    public byte[] getParameters() { return mParameters; }

    public static final Creator<ProdXCapabilityRequest> CREATOR = new Creator<ProdXCapabilityRequest>() {
        @Override public ProdXCapabilityRequest createFromParcel(Parcel in) { return new ProdXCapabilityRequest(in); }
        @Override public ProdXCapabilityRequest[] newArray(int size) { return new ProdXCapabilityRequest[size]; }
    };

    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mDescriptor, flags);
        dest.writeByteArray(mParameters);
    }
}
