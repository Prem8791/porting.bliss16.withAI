package android.app.prodx;

import android.os.Parcel;
import android.os.Parcelable;

public class ProdXExecutionAuthorization implements Parcelable {
    private final String mAuthorizationId;
    private final byte[] mToken;
    private final long mExpiresAt;

    public ProdXExecutionAuthorization(String authorizationId, byte[] token, long expiresAt) {
        mAuthorizationId = authorizationId;
        mToken = token;
        mExpiresAt = expiresAt;
    }

    protected ProdXExecutionAuthorization(Parcel in) {
        mAuthorizationId = in.readString();
        mToken = in.createByteArray();
        mExpiresAt = in.readLong();
    }

    public String getAuthorizationId() { return mAuthorizationId; }
    public byte[] getToken() { return mToken; }
    public long getExpiresAt() { return mExpiresAt; }

    public static final Creator<ProdXExecutionAuthorization> CREATOR = new Creator<ProdXExecutionAuthorization>() {
        @Override public ProdXExecutionAuthorization createFromParcel(Parcel in) { return new ProdXExecutionAuthorization(in); }
        @Override public ProdXExecutionAuthorization[] newArray(int size) { return new ProdXExecutionAuthorization[size]; }
    };

    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAuthorizationId);
        dest.writeByteArray(mToken);
        dest.writeLong(mExpiresAt);
    }
}
