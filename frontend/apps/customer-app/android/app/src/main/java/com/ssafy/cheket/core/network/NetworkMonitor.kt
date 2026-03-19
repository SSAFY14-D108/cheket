package com.ssafy.cheket.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * 네트워크 연결 상태를 실시간으로 감시하는 Flow.
 */
fun Context.observeNetworkConnectivity(): Flow<Boolean> = callbackFlow {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // 현재 상태 즉시 emit
    val currentNetwork = cm.activeNetwork
    val currentCaps = cm.getNetworkCapabilities(currentNetwork)
    val isConnected = currentCaps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    trySend(isConnected)

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(true)
        }

        override fun onLost(network: Network) {
            trySend(false)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            val hasInternet = networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
            trySend(hasInternet)
        }
    }

    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    cm.registerNetworkCallback(request, callback)

    awaitClose {
        cm.unregisterNetworkCallback(callback)
    }
}.distinctUntilChanged()
