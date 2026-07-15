package android.app.prodx;

import android.os.Parcel;
import android.os.Parcelable;

public class ProdXPolicyDecision implements Parcelable {
    private final boolean mAllowed;
    private final String mReason;

    public ProdXPolicyDecision(boolean allowed, String reason) {
        mAllowed = allowed;
        mReason = reason;
    }

    protected ProdXPolicyDecision(Parcel in) {
        mAllowed = in.readBoolean();
        mReason = in.readString();
    }

    public boolean isAllowed() { return mAllowed; }
    public String getReason() { return mReason; }

    public static final Creator<ProdXPolicyDecision> CREATOR = new Creator<ProdXPolicyDecision>() {
        @Override public ProdXPolicyDecision createFromParcel(Parcel in) { return new ProdXPolicyDecision(in); }
        @Override public ProdXPolicyDecision[] newArray(int size) { return new ProdXPolicyDecision[size]; }
    };

    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeBoolean(mAllowed);
        dest.writeString(mReason);
    }
}
