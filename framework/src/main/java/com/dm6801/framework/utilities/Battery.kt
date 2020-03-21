package com.dm6801.framework.utilities

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.dm6801.framework.infrastructure.foregroundApplication

object Battery {

    val percent: Float?
        get() = catch {
            val result =
                foregroundApplication.registerReceiver(
                    null,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                ) ?: return@catch null

            val level = result.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = result.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            if (level == -1 || scale == -1) return@catch null

            level / scale.toFloat()
        }


    val isCharging: Boolean?
        get() = catch {
            val result =
                foregroundApplication.registerReceiver(
                    null,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                ) ?: return@catch null

            val type = result.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

            if (type == -1) return@catch null

            type == BatteryManager.BATTERY_PLUGGED_AC || type == BatteryManager.BATTERY_PLUGGED_USB || type == BatteryManager.BATTERY_PLUGGED_WIRELESS
        }
}