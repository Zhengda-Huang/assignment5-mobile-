package com.cs4520.assignment5.model.database

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository(context: Context) {

    private var productDao: ProductDao
    private val database = ProductDatabase.getInstance(context)

    init {
        productDao = database.productDao()
    }

    suspend fun insertProduct(product: Product) {
        productDao.insert(product)
    }

    // retrieve the product base on the page number
    suspend fun getProducts(page: Int ): List<Product> {
        val pageSize = 30
        val offset = (page - 1) * pageSize
        return productDao.getProductsByPage(pageSize, offset)
    }
}
