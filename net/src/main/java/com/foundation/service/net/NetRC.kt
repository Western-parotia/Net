package com.foundation.service.net

import com.foundation.service.net.utils.log
import com.foundation.service.net.utils.networkIsAvailable
import com.foundation.service.net.utils.transformHttpException
import kotlinx.coroutines.*
import retrofit2.Response


/**
 * 基于 Retrofit+Coroutine的网络请求框架
 * create by zhusw on 6/4/21 18:10
 */
object NetRC {
    private const val TAG = "NetRC"
    private val normalScope = CoroutineScope(SupervisorJob())
    private val uiDispatcher = Dispatchers.Main.immediate
    private val ioDispatcher = Dispatchers.IO

    /**
     * block 作为匿名协程拓展，具备包含子协程的能力
     * 但应该完全避免其中包含独立协程（任何情况下，都不应该使用独立协程嵌套，这样会丧失"父子"协程的控制）
     * [NetStateListener] 作为状态监听器，通常你应该自己实现一个子类，
     * 统一处理状态满足匹配UI展示需要
     *
     * 异常捕获：不管是 withContext(IO) 还是 async(IO) 中发生的异常，最终都会
     * 在根协程的线程环境获取到异常信息。
     * @param block
     * @param state
     * @param tag
     * @param appointScope 如果未指定协程 则会创建一个新的[CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)] 协程
     * @return 如果未指定可自动取消的appointScope，则需要自主控制取消
     */
    fun uiLaunch(
        block: suspend CoroutineScope.() -> Unit,
        state: NetStateListener? = null,
        tag: String = "",
        appointScope: CoroutineScope? = null
    ): Job {
        return launch(block, state, tag, appointScope ?: normalScope, uiDispatcher)
    }

    /**
     * 回调在子线程
     * @param block
     * @param state
     * @param tag
     * @param appointScope
     * @return
     */
    fun ioLaunch(
        block: suspend CoroutineScope.() -> Unit,
        state: NetStateListener? = null,
        tag: String = "",
        appointScope: CoroutineScope? = null
    ): Job {
        return launch(block, state, tag, appointScope ?: normalScope, ioDispatcher)
    }

    private fun launch(
        block: suspend CoroutineScope.() -> Unit,
        state: NetStateListener? = null,
        tag: String = "",
        appointScope: CoroutineScope,
        dispatcher: CoroutineDispatcher
    ): Job {
        val exHandler = CoroutineExceptionHandler { ctx, throwable ->
            throwable.printStackTrace()
            val name: String? = ctx[CoroutineName]?.name
            "$throwable ,ctxName:$name ,thread:${Thread.currentThread().name}".log(TAG)
            if (throwable is CancellationException) {
                state?.onCancel(throwable)
            } else {
                val transformThrowable = transformHttpException(throwable)
                state?.onFailure(transformThrowable)
            }
        }
        val ctx = if (tag.isNotEmpty()) {
            exHandler + CoroutineName(tag)
        } else {
            exHandler
        }

        val job = appointScope.launch(ctx + dispatcher) {
            if (!networkIsAvailable(NetManager.app)) {
                throw NetException.createNetWorkType("网络链接不可用")
            }
            state?.onStart()
            block.invoke(this)
            state?.onSuccess()
        }
        return job
    }


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
        return withContext(Dispatchers.IO) {//异常信息 将在根协程的线程环境捕获
            block.invoke()
        }
    }

    suspend fun <T> withUI(block: suspend () -> T): T {
        return withContext(Dispatchers.Main) {
            block.invoke()
        }
    }
}