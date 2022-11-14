package com.foundation.service.net

import com.foundation.service.net.state.NetException
import com.foundation.service.net.utils.log
import com.foundation.service.net.utils.networkIsAvailable
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.*
import org.json.JSONException
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLException


/**
 * 基于 协程的网络请求框架
 * NetRequestCoroutine 简称 NetRC
 * create by zhusw on 6/4/21 18:10
 */
object NetRC {

    private const val TAG = "NetRC"
    private val uiDispatcher = Dispatchers.Main.immediate
    private val ioDispatcher = Dispatchers.IO

    fun uiLaunch(
        tag: String,
        appointScope: CoroutineScope,
        block: suspend CoroutineScope.() -> Unit
    ): NetFuture {
        return launch(block, tag, appointScope)
    }

    /**
     * 采用惰性启动协程执行任务，掉用NetFuture.start()启动执行
     *
     */
    private fun launch(
        block: suspend CoroutineScope.() -> Unit,
        tag: String,
        appointScope: CoroutineScope,
    ): NetFuture {
        val stateProxy = NetStateProxy()
        val exHandler = CoroutineExceptionHandler { ctx, throwable ->
            val name: String? = ctx[CoroutineName]?.name
            "ctxName:$name ,thread:${Thread.currentThread().name},$throwable ".log(TAG)
            val transformThrowable = transformHttpException(throwable)
            stateProxy.onFailure(tag, transformThrowable)
        }

        val ctx = exHandler + CoroutineName(tag)
        val job = appointScope.launch(ctx, start = CoroutineStart.LAZY) {
            stateProxy.onStart()
            if (!networkIsAvailable(NetManager.app)) {
                throw NetException.createNetWorkType("网络链接不可用")
            }
            block.invoke(this)
            stateProxy.onSuccess()
        }
        return NetFuture(job, stateProxy)
    }

    /**
     * 将过滤网络请求是否成功,不成功将通过抛出异常中断请求
     * @param T
     * @param block
     * @return
     */
    suspend fun <T> withResponse(block: suspend () -> Response<T>): T? {
        val res = withIO(block)
        return when {
            res.isSuccessful -> {
                res.body()
            }
            else -> throw NetException.createResponseType(res)
        }
    }

    suspend fun <T> withIO(block: suspend () -> T): T {
        return withContext(ioDispatcher) {//异常信息 将传递到根协程的线程环境中
            block.invoke()
        }
    }

    suspend fun <T> withUI(block: suspend () -> T): T {
        return withContext(uiDispatcher) {
            block.invoke()
        }
    }
}


/**
 * 匹配属于 http 的异常，包装为[NetException]返回
 * 如果没有匹配到则返回原始异常
 */
internal fun transformHttpException(e: Throwable): Throwable {
    return when (e) {
        is JSONException,
        is JsonSyntaxException -> {
            return NetException.createLocalType("数据解析异常", e)
        }
        is HttpException -> {
            return NetException.createConnectType("Http 异常 code:${e.code()} msg:${e.message()}", e)
        }
        is UnknownHostException -> {
            return NetException.createConnectType("访问的目标主机不存在", e)
        }
        is SSLException -> {
            return NetException.createConnectType("无法与目标主机建立链接", e)
        }
        is SocketTimeoutException,
        is ConnectException -> {
            return NetException.createConnectType("网络链接异常", e)
        }
        is TimeoutException -> {
            return NetException.createConnectType("网络链接超时", e)
        }
        else -> {
            e
        }
    }
}