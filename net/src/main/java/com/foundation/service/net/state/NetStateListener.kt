package com.foundation.service.net.state

/**
 * 网络请求状态回调
 * create by zhusw on 5/25/21 16:11
 */
interface NetStateListener {
    fun onStart()
    fun onSuccess()
    fun onFailure(tagName: String, e: Throwable)
}