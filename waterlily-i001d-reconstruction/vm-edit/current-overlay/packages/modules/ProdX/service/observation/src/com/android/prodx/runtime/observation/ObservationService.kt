package com.android.prodx.runtime.observation

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ObservationService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY
}
