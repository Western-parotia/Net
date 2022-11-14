package com.foundation.app.simple.demo.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.foundation.app.simple.demo.base.BaseWanAndroidVM
import com.foundation.app.simple.demo.home.data.BannerEntity
import com.foundation.app.simple.demo.home.data.NewsFeedInfo
import com.foundation.app.simple.demo.net.WanAndroidNetStateHandler
import com.foundation.app.simple.demo.net.api.WanAndroidService
import com.foundation.service.net.NetManager
import com.foundation.service.net.getApiService

/**
 *
 */
class HomeVM : BaseWanAndroidVM() {

    private val homeApi = NetManager.getApiService<WanAndroidService>()


    /**
     * 核心架构 思想：保证单一可信源
     * view层只能订阅状态，不可修改状态
     */
    private val _bannerData = MutableLiveData<List<BannerEntity>>()
    val bannerData: LiveData<List<BannerEntity>> = _bannerData

    private val _newsLiveData = MutableLiveData<List<NewsFeedInfo>>()
    val newsLiveData: LiveData<List<NewsFeedInfo>> = _newsLiveData

    fun loadBanner() {

        netLaunch("加载banner") {
            val data = withBusiness {
                homeApi.getBanner()
            }
            _bannerData.value = data
        }.onStart {

        }.onSuccess {

        }.onFailure { tagName, e ->

        }.start()

        //-----------------

        netLaunch("加载列表") {
            _newsLiveData.value = withBusiness {
                homeApi.getNews(pageCount)
            }.datas
        }.start(WanAndroidNetStateHandler(stateLiveData = _loadEventLiveData))

        netLaunch("加载列表") {
            _newsLiveData.value = withBusiness {
                homeApi.getNews(pageCount)
            }.datas
        }.startWithLoading()

    }

    private var pageCount = -1

    private val _cleanAdapterLiveData = MutableLiveData<Unit>()
    val cleanAdapterLiveData: LiveData<Unit> = _cleanAdapterLiveData

    fun loadNews(refresh: Boolean = true) {
        if (refresh) {
            _cleanAdapterLiveData.value = Unit
            pageCount = 0
        } else {
            pageCount++
        }

        netLaunch("加载列表") {
            _newsLiveData.value = withBusiness {
                homeApi.getNews(pageCount)
            }.datas
        }.start(WanAndroidNetStateHandler(stateLiveData = _loadEventLiveData))

    }
}