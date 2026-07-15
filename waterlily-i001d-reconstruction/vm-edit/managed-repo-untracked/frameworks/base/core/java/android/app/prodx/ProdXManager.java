package android.app.prodx;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;

/**
 * Internal framework entry point for the ProdX runtime.
 *
 * @hide
 */
public class ProdXManager {
    private static final String TAG = "ProdXManager";
    private final Context mContext;
    private IProdXAuthority mService;

    public ProdXManager(Context context) {
        mContext = context;
    }

    private IProdXAuthority getService() {
        if (mService == null) {
            android.os.IBinder b = ServiceManager.getService("prodx_authority");
            if (b != null) mService = IProdXAuthority.Stub.asInterface(b);
        }
        return mService;
    }

    public ProdXMode getMode() {
        IProdXAuthority s = getService();
        if (s == null) return ProdXMode.DISABLED;
        try { return s.getMode(); } catch (RemoteException e) { return ProdXMode.DISABLED; }
    }

    public ProdXHealth getHealth() {
        IProdXAuthority s = getService();
        if (s == null) return new ProdXHealth(false, "service_unavailable");
        try { return s.getHealth(); } catch (RemoteException e) { return new ProdXHealth(false, "remote_error"); }
    }
}
