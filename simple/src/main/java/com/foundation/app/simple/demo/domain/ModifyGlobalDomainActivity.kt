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
 * 修改全局域名
 * create by zhusw on 5/31/21 16:03
 */
class ModifyGlobalDomainActivity : BaseActivity() {

    val vb by lazyVB<ActDynamicDomainBinding>()
    override fun getContentVB(): ViewBinding = vb

    override fun init(savedInstanceState: Bundle?) {
        val okHttpClient = OkHttpClient.Builder().addDynamicDomainSkill()
            .addInterceptor(HttpLoggingInterceptor {
                Log.i("domain==", it)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://www.sogou.com/")
            .build()
        NetManager.init(retrofit, application, BuildConfig.DEBUG)

        NetManager.addUrlChangedListener(object : OnUrlChanged {
            override fun onUrlChangeBefore(oldUrl: HttpUrl?, domainName: String?) {
                MainScope().launch {
                    vb.etRes.setText("将改变 urlKey:$domainName")
                    vb.etResSpecial.setText("将改变 urlKey:$domainName")
                }
            }

            override fun onUrlChanged(newUrl: HttpUrl?, oldUrl: HttpUrl?) {
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
                    retrofit.create(GlobalSearchApi::class.java)
                        .globalSearch()
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

        //-----------------special-----------------

        vb.btnRequestSpecial.setOnClickListener {
            vb.etResSpecial.setText("准备请求")
            val exHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
                "eror:${throwable.message}".log("domain====")
                throwable.printStackTrace()
            }
            MainScope().launch(exHandler) {
                val res = withContext(Dispatchers.IO) {
                    retrofit.create(GlobalSearchApi::class.java)
                        .specialSearch()
                }

                val request = res.raw().request()
                vb.etResSpecial.setText(
                    "请求url:${request.url()}" +
                            " \n " +
                            "body=${res.body()?.string()}"
                )
            }

        }

        vb.btnBaiduSpecial.setOnClickListener {
            NetManager.putDomain(GlobalSearchApi.DOMAIN_KEY_SEARCH, "https://www.baidu.com")
        }
        vb.btnBingSpecial.setOnClickListener {
            NetManager.putDomain(GlobalSearchApi.DOMAIN_KEY_SEARCH, "https://www.bing.com")

        }
    }

    override fun bindData() {


    }
}