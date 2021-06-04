package com.foundation.service.net

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import retrofit2.Response

/**
 * 链式处理请求解析 业务>Retrofit>netIo
 * 网络加载状态监听
 * 异常捕获与分类
 * create by zhusw on 5/25/21 15:19
 */
open class NetViewModel : ViewModel() {
    private val TAG = "NetViewModel"
    protected val _loadEventLiveData = MutableLiveData<NetLoadingEvent>()
    val loadEventLiveData: LiveData<NetLoadingEvent> = _loadEventLiveData

    /**
     * 使用viewModelScope 协程
     * 不需要取消
     */
    fun netLaunch(
        block: suspend CoroutineScope.() -> Unit,
        state: NetStateListener? = null,
        tag: String = ""
    ) {
        NetRC.uiLaunch(block, state, tag, viewModelScope)
    }

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