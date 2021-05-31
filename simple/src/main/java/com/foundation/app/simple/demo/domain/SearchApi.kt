package com.foundation.app.simple.demo.domain

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

/**
 * create by zhusw on 5/31/21 16:09
 */
interface SearchApi {
    @GET("/")
    suspend fun search(): Response<ResponseBody>
}