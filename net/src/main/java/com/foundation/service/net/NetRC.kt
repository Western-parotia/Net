package com.foundation.service.net

import android.os.Handler
import android.os.Looper
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
    private val handler = Handler(Looper.getMainLooper())

    private const val TAG = "NetRC"
    private val uiDispatcher = Dispatchers.Main.immediate
    private val ioDispatcher = Dispatchers.IO

    /**
     * 回调在主线程
     * block 作为匿名协程拓展，具备包含子协程的能力
     * 但应该完全避免其中包含独立协程（任何情况下，都不应该使用独立协程嵌套，这样会丧失"父子"协程的控制）
     * [NetStateListener] 作为状态监听器，通常你应该自己实现一个子类，
     * 统一处理状态满足匹配UI展示需要
     *
     * 异常捕获：不管是 withContext(IO) 还是 async(IO) 中发生的异常，最终都会
     * 在根协程的线程环境获取到异常信息。
     * @param block
     * @param state
     * @param tag 此次的协程任务名称
     * @param appointScope 如果未指定协程 则会创建一个新的[CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)] 协程
     * @return 如果未指定可自动取消的appointScope，则需要自主控制取消
     */
    fun uiLaunch(
        state: NetStateListener?,
        tag: String?,
        appointScope: CoroutineScope,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return launch(block, state, tag, appointScope, uiDispatcher)
    }

    /**
     * 参数同[uiLaunch] 一致，但是回调在子线程
     */
    fun ioLaunch(
        state: NetStateListener?,
        tag: String?,
        appointScope: CoroutineScope,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return launch(block, state, tag, appointScope, ioDispatcher)
    }

    private fun launch(
        block: suspend CoroutineScope.() -> Unit,
        state: NetStateListener? = null,
        tag: String?,
        appointScope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
    ): Job {
        val exHandler = CoroutineExceptionHandler { ctx, throwable ->
            val name: String? = ctx[CoroutineName]?.name
            "ctxName:$name ,thread:${Thread.currentThread().name},$throwable ".log(TAG)
            val transformThrowable = transformHttpException(throwable)
            //针对UI线程场景发起协程，保持异常回调也回到UI线程
            if (dispatcher == uiDispatcher) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    handler.post {
                        state?.onFailure(tag, transformThrowable)
                    }
                } else {
                    state?.onFailure(tag, transformThrowable)
                }
            }
        }

        val ctx = if (tag.isNullOrEmpty()) exHandler else exHandler + CoroutineName(tag)
        return appointScope.launch(ctx + dispatcher) {
            state?.onStart()
            if (!networkIsAvailable(NetManager.app)) {
                throw NetException.createNetWorkType("网络链接不可用")
            }
            block.invoke(this)
            state?.onSuccess()
        }
    }

    /**
     * 将过滤网络请求是否成功
     * @param T
     * @param block
     * @return
     */
    suspend fun <T> withResponse(block: suspend () -> Response<T>): T? {
        val res = withIO(block) //Response<BaseApiResponse<List<YourData>>>
        return when {
            res.isSuccessful -> {
                res.body()
            }
            else -> throw NetException.createResponseType(res)
        }
    }

    suspend fun <T> withIO(block: suspend () -> T): T {
        return withContext(ioDispatcher) {//异常信息 将在根协程的线程环境捕获
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
            return NetException.createNormalType("数据解析异常", e)
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