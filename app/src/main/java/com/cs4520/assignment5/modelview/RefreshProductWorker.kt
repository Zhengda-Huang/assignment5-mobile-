package com.cs4520.assignment5.modelview

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cs4520.assignment5.model.api.ProductApiRequest
import com.cs4520.assignment5.model.database.Product
import com.cs4520.assignment5.model.database.ProductRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.logging.Logger
class RefreshProductWorker (context: Context, workerParams: WorkerParameters) : Worker(context,
    workerParams
) {
    private val productApi: ProductApiRequest = ProductApiRequest()
    private val logger = Logger.getLogger("MyLogger")
    private var repository: ProductRepository

    init {
        repository = ProductRepository(context)
    }

    override fun doWork(): Result {
        return try {
            var outputData: Data? = null // Declare outputData outside coroutine scope
            runBlocking { // Use runBlocking to block the current thread until coroutine completes
                CoroutineScope(Dispatchers.IO).launch {
                    logger.info("Fetching products in background...")
                    try {
                        val products = fetchProducts()
                        logger.info("Products fetched")
                        logger.info("Start storing products")
                        products.forEach { product ->
                            repository.insertProduct(product)
                        }

                        logger.info("Finish storing products")

                        // Create an output data object and put the products in it
                        outputData = Data.Builder().putString("products", Gson().toJson(products)).build()
                    } catch (e: Exception) {
                        logger.warning(e.toString())
                    }
                }.join() // Wait for the coroutine to complete
            }

            // Return the result with outputData
            Result.success(outputData ?: Data.EMPTY)
        } catch (e: Exception) {
            logger.warning("Failed to fetch products in background: ${e.message}")
            Result.failure()
        }
    }
    private suspend fun fetchProducts(): List<Product> {
        return withContext(Dispatchers.IO) {
            try {
                productApi.fetchProducts(1)
            } catch (e: Exception) {
                logger.warning("Failed to fetch products: ${e.message}")
                emptyList()
            }
        }
    }


}