package android.app.prodx;

import android.app.prodx.ProdXHealth;
import android.app.prodx.ProdXMode;

interface IProdXSettingsMediator {
    ProdXHealth getAuthorityHealth();
    ProdXMode getCurrentMode();
}
