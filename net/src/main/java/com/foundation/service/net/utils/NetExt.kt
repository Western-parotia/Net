package com.foundation.service.net.utils

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.foundation.service.net.NetManager
import com.foundation.service.urlmanager.retrofiturlmanager.RetrofitUrlManager
import okhttp3.OkHttpClient

/**
 * create by zhusw on 5/26/21 11:56
 */

private const val TAG = "_Net_"

internal fun String.log(secondTag: String = "") {
    if (NetManager.debug) {
        Log.i(TAG, "$secondTag:$this")
    }
}

fun OkHttpClient.Builder.addDynamicDomainSkill(): OkHttpClient.Builder {
    return RetrofitUrlManager.getInstance().with(this)
}

internal fun networkIsAvailable(context: Context): Boolean {
    val cm = ContextCompat.getSystemService(
        context,
        ConnectivityManager::class.java
    )
    cm?.let {
        return it.activeNetworkInfo?.isAvailable ?: false
    }
    return false
}

