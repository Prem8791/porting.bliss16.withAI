package android.app.prodx;

import android.os.Parcel;
import android.os.Parcelable;

public class ProdXGrant implements Parcelable {
    private final String mGrantId;
    private final int mUserId;
    private final String mPackageName;
    private final String mCapabilityId;
    private final long mGrantedAt;
    private final boolean mActive;

    public ProdXGrant(String grantId, int userId, String packageName, String capabilityId, long grantedAt, boolean active) {
        mGrantId = grantId;
        mUserId = userId;
        mPackageName = packageName;
        mCapabilityId = capabilityId;
        mGrantedAt = grantedAt;
        mActive = active;
    }

    protected ProdXGrant(Parcel in) {
        mGrantId = in.readString();
        mUserId = in.readInt();
        mPackageName = in.readString();
        mCapabilityId = in.readString();
        mGrantedAt = in.readLong();
        mActive = in.readBoolean();
    }

    public String getGrantId() { return mGrantId; }
    public int getUserId() { return mUserId; }
    public String getPackageName() { return mPackageName; }
    public String getCapabilityId() { return mCapabilityId; }
    public long getGrantedAt() { return mGrantedAt; }
    public boolean isActive() { return mActive; }

    public static final Creator<ProdXGrant> CREATOR = new Creator<ProdXGrant>() {
        @Override public ProdXGrant createFromParcel(Parcel in) { return new ProdXGrant(in); }
        @Override public ProdXGrant[] newArray(int size) { return new ProdXGrant[size]; }
    };

    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mGrantId);
        dest.writeInt(mUserId);
        dest.writeString(mPackageName);
        dest.writeString(mCapabilityId);
        dest.writeLong(mGrantedAt);
        dest.writeBoolean(mActive);
    }
}
