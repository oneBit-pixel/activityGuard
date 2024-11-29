package com.activityGuard.confuseapp

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by DengLongFei
 * 2024/11/28
 */
class MyService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}