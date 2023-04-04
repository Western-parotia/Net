package com.foundation.app.simple.demo.home

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.foundation.app.simple.architecture.BaseActivity
import com.foundation.app.simple.databinding.ActHomeWanandroidBinding
import com.foundation.app.simple.demo.home.data.NewsFeedInfo
import com.foundation.app.simple.toast
import com.foundation.service.net.state.NetLoadingEvent
import com.foundation.widget.loading.LoadingConstraintLayout
import com.foundation.widget.loading.StreamerConstraintLayout
import com.foundation.widget.loading.StreamerPageLoadingAdapter

/**
 * create by zhusw on 5/20/21 11:33
 */
class HomeActivity : BaseActivity() {

    private val viewBinding by lazyVB<ActHomeWanandroidBinding>()

    override fun getContentVB(): ViewBinding = viewBinding

    private val homeVM by lazyActivityVM<HomeVM>()

    private val adapter = NewsAdapter()
    override fun init(savedInstanceState: Bundle?) {
        viewBinding.rlNews.adapter = adapter
        viewBinding.rlNews.layoutManager = LinearLayoutManager(this)
        adapter.setOnItemClickListener { adapter, view, position ->
            val data = adapter.getItem(position) as NewsFeedInfo
            "点击：${data.title}".toast()

        }
        initLoading(viewBinding.contentLoading)
        viewBinding.contentLoading.stop()
        viewBinding.btnInit.setOnClickListener {
            homeVM.loadBanner()
            homeVM.loadNews(true)
        }
        viewBinding.btnListMore.setOnClickListener {
            homeVM.loadNews(false)
        }
        viewBinding.btnListNew.setOnClickListener {
            homeVM.loadNews(true)
        }
    }

    override fun bindData() {
        homeVM.loadEventLiveData.observe(this) {
            when (it) {
                is NetLoadingEvent.StartEvent -> {
                    viewBinding.contentLoading.asLoading().showLoading()
                }
                is NetLoadingEvent.StopEvent -> {
                    viewBinding.contentLoading.asLoading().stop()
                }
                is NetLoadingEvent.ErrorEvent -> {
                    viewBinding.contentLoading.asLoading().stop()
                    "${it.msg}:${it.code}".toast()
                }
            }
        }
        homeVM.bannerData.observe(this) {
            Glide.with(this).load(it[2].imagePath)
                .into(viewBinding.ivBanner)
            viewBinding.tvBannerTitle.text = it[2].title
        }

        homeVM.cleanAdapterLiveData.observe(this) {
            adapter.setNewInstance(null)
        }
        homeVM.newsLiveData.observe(this) {
            adapter.addData(it)
        }
        homeVM.loadBanner()

    }


    private fun initLoading(loadingView: LoadingConstraintLayout) {
        val streamerView = StreamerConstraintLayout(this)
        val tv = TextView(this)
        tv.text = "正在加载..."
        tv.textSize = 10F.dp
        tv.typeface = Typeface.DEFAULT_BOLD
        tv.setTextColor(Color.parseColor("#334D4D"))
        val lp = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        streamerView.addView(tv, lp)
        streamerView.streamerColor = Color.parseColor("#E3E8E8")
        streamerView.animDuration = 1000L
        streamerView.skipCount = 0
        streamerView.streamerWidth = 20F.dp
        loadingView.setLoadingAdapter(StreamerPageLoadingAdapter(streamerView))
    }
}

internal val Float.dp get() = this * Resources.getSystem().displayMetrics.density + 0.5F
internal val Int.dp get() = this.toFloat().dp.toInt()