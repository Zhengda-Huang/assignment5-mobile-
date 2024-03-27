package com.cs4520.assignment5.model.api

import com.cs4520.assignment5.model.database.Product
import java.util.logging.Logger

class ProductApiRequest() {
    private val apiService = ApiClient.ApiClient.apiService
    private val logger = Logger.getLogger("MyLogger")

    // fetch the product from the api
    suspend fun fetchProducts(page: Int): List<Product> {
        try {
            logger.info("Start fetching products")
            val response = apiService.getProducts(page)
            if (response.isSuccessful) {
                val productList = response.body()
                if (productList != null) {
                    logger.info("Products fetched successfully")
                    return productList.distinctBy { it.name }
                } else {
                    logger.warning("Response body is null")
                }
            } else {
                logger.warning("Failed to fetch products: ${response.code()}")
            }
        } catch (e: Exception) {
            logger.severe("Failed to fetch products: ${e.message}")
        }

        return emptyList()
    }

}
