package com.foundation.app.simple.demo.domain

import android.os.Bundle
import android.util.Log
import androidx.viewbinding.ViewBinding
import com.foundation.app.simple.BuildConfig
import com.foundation.app.simple.architecture.BaseActivity
import com.foundation.app.simple.databinding.ActDynamicDomainBinding
import com.foundation.app.simple.log
import com.foundation.service.net.NetManager
import com.foundation.service.net.OnUrlChanged
import com.foundation.service.net.utils.addDynamicDomainSkill
import kotlinx.coroutines.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

/**
 * create by zhusw on 5/31/21 16:03
 */
class DynamicDomainActivity : BaseActivity() {

    val vb by lazyVB<ActDynamicDomainBinding>()
    override fun getContentVB(): ViewBinding = vb

    override fun init(savedInstanceState: Bundle?) {
        val okHttpClient = OkHttpClient.Builder().addDynamicDomainSkill()
            .addInterceptor(HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.i("domain==", it)
            }).apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://www.baidu.com")
            .build()
        NetManager.init(retrofit, application, BuildConfig.DEBUG)

        NetManager.addUrlChangedListener(object : OnUrlChanged {
            override fun onUrlChangeBefore(oldUrl: HttpUrl?, domainName: String?) {
                MainScope().launch {
                    vb.etRes.setText("url 将改变")
                }
            }

            override fun onUrlChanged(newUrl: HttpUrl?, oldUrl: HttpUrl?) {
                MainScope().launch {
                    vb.tvDomain.text = "当前url:${newUrl?.url()}"
                }
            }
        })

        vb.btnRequest.setOnClickListener {
            vb.etRes.setText("准备请求")
            val exHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
                "eror:${throwable.message}".log("domain====")
                throwable.printStackTrace()
            }
            MainScope().launch(exHandler) {
                val res = withContext(Dispatchers.IO) {
                    retrofit.create(SearchApi::class.java)
                        .search()
                }

                val request = res.raw().request()
                vb.etRes.setText(
                    "请求url:${request.url()}" +
                            " \n " +
                            "body=${res.body()?.string()}"
                )
            }

        }

        vb.btnBaidu.setOnClickListener {
            NetManager.setGlobalDomain("https://www.baidu.com")
        }
        vb.btnBing.setOnClickListener {
            NetManager.setGlobalDomain("https://www.bing.com")
        }

    }

    override fun bindData() {


    }
}