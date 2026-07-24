package com.spiritfenix.stromr.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface RssApi {
    @GET
    suspend fun fetchFeed(@Url feedUrl: String): ResponseBody
}