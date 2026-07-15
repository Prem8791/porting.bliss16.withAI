package com.android.prodx.provider.test

import android.app.Service
import android.content.Intent
import android.os.IBinder

class NoOpTestProviderService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
