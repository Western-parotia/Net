package com.foundation.service.net

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foundation.service.net.state.NetStateListener
import kotlinx.coroutines.CoroutineScope
import retrofit2.Response

/**
 * 自动设置 viewModelScope 作为 网络请求的根协程域
 * viewModelScope 将自动跟随 ViewModel管理 其中的任务的取消
 * create by zhusw on 5/25/21 15:19
 */
open class NetViewModel : ViewModel() {

    fun netLaunch(tag: String, block: suspend CoroutineScope.() -> Unit): NetFuture =
        NetRC.uiLaunch(tag, viewModelScope, block)

    protected suspend fun <T> withResponse(block: suspend () -> Response<T>): T? {
        return NetRC.withResponse(block)
    }

    protected suspend fun <T> withIO(block: suspend () -> T): T {
        return NetRC.withIO(block)
    }

    protected suspend fun <T> withUI(block: suspend () -> T): T {
        return NetRC.withUI(block)
    }
}