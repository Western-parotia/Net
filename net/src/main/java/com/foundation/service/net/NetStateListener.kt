package com.foundation.service.net

/**
 * 网络请求状态回调
 * create by zhusw on 5/25/21 16:11
 */
interface NetStateListener {
    fun onStart()
    fun onSuccess()

    @Deprecated("请重写2参的，带tag更方便使用")
    fun onFailure(e: Throwable) {
    }

    fun onFailure(tagName: String?, e: Throwable) {
        onFailure(e)
    }
}