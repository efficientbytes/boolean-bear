package app.efficientbytes.androidnow.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import androidx.lifecycle.LiveData

class ConnectivityListener(context: Context) :
    LiveData<Boolean>() {

    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private lateinit var networkConnectionCallback: ConnectivityManager.NetworkCallback

    private fun connectivityManagerCallback(): ConnectivityManager.NetworkCallback {
        networkConnectionCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                postValue(false)
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                postValue(true)
            }
        }
        return networkConnectionCallback
    }

    private fun updateNetworkConnection() {
        val activeNetworkConnection: NetworkInfo? = connectivityManager.activeNetworkInfo
        postValue(activeNetworkConnection?.isConnected == true)
    }

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateNetworkConnection()
        }
    }

    override fun onActive() {
        super.onActive()
        updateNetworkConnection()
        connectivityManager.registerDefaultNetworkCallback(connectivityManagerCallback())
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(connectivityManagerCallback())
    }

    fun isInternetAvailable(): Boolean {
        val internetAvailability: NetworkInfo? = connectivityManager.getActiveNetworkInfo()
        return internetAvailability != null && internetAvailability.isAvailable && internetAvailability.isConnected
    }

}