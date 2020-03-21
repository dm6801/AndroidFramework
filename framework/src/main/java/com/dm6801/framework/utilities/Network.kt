package com.dm6801.framework.utilities

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.getSystemService
import com.dm6801.framework.infrastructure.foregroundApplication

object Network {

    private val connectivityManager: ConnectivityManager? get() = foregroundApplication.getSystemService()
    private val wifiManager: WifiManager? get() = foregroundApplication.getSystemService()

    val SSID: String?
        get() {
            return wifiManager?.connectionInfo?.ssid?.removePrefix("\"")?.removeSuffix("\"")
        }

    val RSSI: Int?
        get() {
            return wifiManager?.connectionInfo?.rssi
        }

    @Suppress("DEPRECATION")
    val isConnected: Boolean
        get() {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val capabilities =
                        connectivityManager?.getNetworkCapabilities(connectivityManager?.activeNetwork)
                    capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true ||
                            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true ||
                            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true
                } else {
                    return connectivityManager?.activeNetworkInfo?.isConnected == true
                }
            } catch (_: Exception) {
                false
            }
        }

    fun getSignalStrength(scale: Int): Int? {
        return RSSI?.let { WifiManager.calculateSignalLevel(it, scale) }
    }
}