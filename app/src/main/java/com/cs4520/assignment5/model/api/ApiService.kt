package com.cs4520.assignment5.model.api

import com.cs4520.assignment5.model.database.Product
import retrofit2.http.GET
import retrofit2.Response
import retrofit2.http.Query

interface ApiService {
    @GET("random/")
    suspend fun getProducts(@Query("page") page: Int? = null): Response<List<Product>>
}