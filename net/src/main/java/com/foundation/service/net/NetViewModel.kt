package com.foundation.service.net

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

    /**
     * 使用viewModelScope 协程
     * 不需要取消
     */
    @Deprecated(message = "使用另一个重载更丝滑", replaceWith = ReplaceWith("netLaunch(NetStateListener,String){\n}"))
    fun netLaunch(
        block: suspend CoroutineScope.() -> Unit,
        state: NetStateListener?,
        tag: String?
    ) {
        NetRC.uiLaunch(state, tag, viewModelScope, block)
    }

    /**
     * 使用viewModelScope 协程
     * 不需要取消
     */
    fun netLaunch(
        state: NetStateListener?,
        tag: String?,
        block: suspend CoroutineScope.() -> Unit
    ) {
        NetRC.uiLaunch(state, tag, viewModelScope, block)
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