package com.foundation.service.net.utils

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.foundation.service.net.NetException
import com.foundation.service.net.NetManager
import com.foundation.service.urlmanager.retrofiturlmanager.RetrofitUrlManager
import com.google.gson.JsonSyntaxException
import okhttp3.OkHttpClient
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLException

/**
 * create by zhusw on 5/26/21 11:56
 */
private const val TAG = "base-net"
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

