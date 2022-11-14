package com.foundation.app.simple.demo.net

import androidx.lifecycle.MutableLiveData
import com.foundation.app.simple.log
import com.foundation.service.net.state.NetException
import com.foundation.service.net.state.NetLoadingEvent
import com.foundation.service.net.state.NetStateListener

/**
 * create by zhusw on 5/26/21 14:06
 */
class WanAndroidNetStateHandler(
    private val stateLiveData: MutableLiveData<NetLoadingEvent>
) : NetStateListener {
    override fun onStart() {
        stateLiveData.value = NetLoadingEvent.StartEvent
    }

    override fun onSuccess() {
        stateLiveData.value = NetLoadingEvent.StopEvent
    }

    override fun onFailure(tagName: String, e: Throwable) {
        handlerNetException(e)
    }

    private fun handlerNetException(e: Throwable) {
        when (e) {
            is NetException -> {
                stateLiveData.value = NetLoadingEvent.ErrorEvent(e.netCode, e.netMsg)
                "NetException: $e".log("net--")
            }
            is WanAndroidResException -> {
                stateLiveData.value = NetLoadingEvent.ErrorEvent(e.code, e.msg)
                "WanAndroidResException: $e".log("net--")
            }
            else -> {
                stateLiveData.value = NetLoadingEvent.ErrorEvent(-1, "网络层未知错误")
                "else: $e".log("net--")
            }
        }

    }
}