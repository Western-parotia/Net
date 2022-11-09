package com.foundation.service.net

import android.app.Application
import com.foundation.service.urlmanager.retrofiturlmanager.RetrofitUrlManager
import com.foundation.service.urlmanager.retrofiturlmanager.onUrlChangeListener
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 网络管理器：init 之后 可获取 apiService
 * 提供接口对象缓存
 * 动态域名替换
 * create by zhusw on 5/25/21 10:52
 */
object NetManager : INetManagerSkill {

    private val onUrlChangedList = arrayListOf<OnUrlChanged>()
    private val onUrlChanged = object : OnUrlChanged {
        override fun onUrlChangeBefore(oldUrl: HttpUrl?, domainName: String?) {
            onUrlChangedList.forEach {
                it.onUrlChangeBefore(oldUrl, domainName)
            }
        }

        override fun onUrlChanged(newUrl: HttpUrl?, oldUrl: HttpUrl?) {
            onUrlChangedList.forEach {
                it.onUrlChanged(newUrl, oldUrl)
            }
        }
    }

    private val lock = Any()

    internal var debug = BuildConfig.DEBUG
        private set

    private val skill: INetManagerSkill by lazy {
        UrkSkill(onUrlChanged)
    }

    private lateinit var retrofit: Retrofit

    private val initState = AtomicBoolean(false)

    internal lateinit var app: Application
        private set

    private val cacheMap by lazy {
        mutableMapOf<Class<*>, Any>()
    }

    fun addDynamicDomainSkill(build: OkHttpClient.Builder): OkHttpClient.Builder {
        return RetrofitUrlManager.getInstance().with(build)
    }

    /**
     * 不保证多线程安全（这无必要），但是会以最早传入的对象为准
     * 参考 lazy(PUBLICATION 模式)
     */
    fun init(retrofit: Retrofit, app: Application, debug: Boolean) {
        if (!initState.get()) {
            initState.set(true)
            this.retrofit = retrofit
            this.debug = debug
            this.app = app
        }
    }

    fun <T : Any> getApiService(clz: Class<T>): T {
        return loadService(clz)
    }

    /**
     * 对运行时创建的service对象缓存
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> loadService(clz: Class<T>): T {
        var service: Any? = cacheMap[clz]
        if (service == null) {
            synchronized(lock) {
                service = cacheMap[clz]
                if (service == null) {
                    service = retrofit.create(clz)
                    cacheMap[clz] = service as T
                }
            }
        }
        return service as T
    }

    fun addUrlChangedListener(o: OnUrlChanged): Boolean {
        return onUrlChangedList.add(o)
    }

    fun removeChangedListener(o: OnUrlChanged): Boolean {
        return onUrlChangedList.remove(o)
    }

    /**
     * 按key 替换域名
     */
    override fun putDomain(domainKey: String, domainUrl: String) {
        skill.putDomain(domainKey, domainUrl)
    }

    /**
     * 替换全局域名
     */
    override fun setGlobalDomain(domain: String) {
        skill.setGlobalDomain(domain)
    }

}

inline fun <reified T : Any> NetManager.getApiService(): T {
    return getApiService(T::class.java)
}

internal class UrkSkill(onUrlChanged: OnUrlChanged) : INetManagerSkill {

    init {
        RetrofitUrlManager.getInstance().registerUrlChangeListener(onUrlChanged)
    }

    override fun putDomain(domainKey: String, domainUrl: String) {
        RetrofitUrlManager.getInstance().putDomain(domainKey, domainUrl)
    }

    override fun setGlobalDomain(domain: String) {
        RetrofitUrlManager.getInstance().setGlobalDomain(domain)
    }

}

interface OnUrlChanged : onUrlChangeListener