package app.efficientbytes.booleanbear.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.util.Log
import androidx.lifecycle.LiveData

class ConnectivityListener(context: Context) :
    LiveData<Boolean>() {

    private var connectivityManager: ConnectivityManager? = null
    private var networkConnectionCallback: ConnectivityManager.NetworkCallback? = null

    init{
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

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
        return networkConnectionCallback as ConnectivityManager.NetworkCallback
    }

    private fun updateNetworkConnection() {
        val activeNetworkConnection: NetworkInfo? = connectivityManager?.activeNetworkInfo
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
        connectivityManager?.registerDefaultNetworkCallback(connectivityManagerCallback())
    }

    override fun onInactive() {
        super.onInactive()
        if (networkConnectionCallback!=null&&connectivityManager!=null){
            try{
                connectivityManager?.unregisterNetworkCallback(connectivityManagerCallback())
                networkConnectionCallback = null
            }catch (e : Exception){
                Log.i("Connectivity Listener","Error is ${e.message}")
            }
        }
    }

    fun isInternetAvailable(): Boolean {
        val internetAvailability: NetworkInfo? = connectivityManager?.getActiveNetworkInfo()
        return internetAvailability != null && internetAvailability.isAvailable && internetAvailability.isConnected
    }

}