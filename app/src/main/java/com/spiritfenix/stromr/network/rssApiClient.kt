package com.spiritfenix.stromr.network

import retrofit2.Retrofit

object rssApiClient {
    val api: RssApi by lazy {//"by lazy" means that the object is created only when it's necessary then cached for future use
        Retrofit.Builder()
            .baseUrl("https://example.com/") // required by Retrofit, unused since @Url overrides it
            .build()
            .create(RssApi::class.java)
    }
}