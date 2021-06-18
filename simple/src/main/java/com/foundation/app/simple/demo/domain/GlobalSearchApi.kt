package com.foundation.app.simple.demo.domain

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

/**
 * create by zhusw on 5/31/21 16:09
 */
interface GlobalSearchApi {
    companion object {
        const val DOMAIN_KEY_SEARCH = "searchDomain"
    }

    @GET("/")
    suspend fun globalSearch(): Response<ResponseBody>

    @Headers("Domain-Name: $DOMAIN_KEY_SEARCH")//searchDomain 是替换域名时用于匹配接口的KEY
    @GET("/")
    suspend fun specialSearch(): Response<ResponseBody>
}