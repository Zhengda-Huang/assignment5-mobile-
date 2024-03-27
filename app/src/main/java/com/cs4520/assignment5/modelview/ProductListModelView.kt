package com.cs4520.assignment5.modelview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.cs4520.assignment5.model.api.ProductApiRequest
import com.cs4520.assignment5.model.database.Product
import com.cs4520.assignment5.model.database.ProductRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class ProductListViewModel(application: Application, private val workManager: WorkManager) : AndroidViewModel(application) {
    private val productApi: ProductApiRequest = ProductApiRequest()
    private val logger = Logger.getLogger("MyLogger")

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error

    private val _page = MutableStateFlow<Int?>(null)
    val page: StateFlow<Int?> = _page

    private var repository: ProductRepository
    val constraints= Constraints.Builder ()
        .setRequiresCharging (false)
        .setRequiredNetworkType (NetworkType. CONNECTED)
        .build()
    init {
        viewModelScope.launch {
            page.collect { page ->
                fetchProducts()
            }
        }

        val myWorkRequest: WorkRequest = PeriodicWorkRequestBuilder<RefreshProductWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "product_refresh_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            myWorkRequest as PeriodicWorkRequest
        )

        initalProductFetching()
        repository = ProductRepository(application.applicationContext)
    }

    fun initalProductFetching(){
        val refreshWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<RefreshProductWorker>()
                .setConstraints(constraints)
                .build()

        workManager.enqueue(refreshWorkRequest)
        workManager.getWorkInfoByIdLiveData(refreshWorkRequest.id).observeForever { workInfo ->
            if (workInfo != null) {
                when (workInfo.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        logger.info("Work request succeeded")
                        val outputData = workInfo.outputData
                        val productsJson = outputData.getString("products")
                        if (productsJson != null){
                            val products: List<Product> = convertJsonToProducts(productsJson)
                            _loading.value = false
                            _products.value = products
                            _error.value = false
                        }
                    }
                    WorkInfo.State.FAILED -> {
                        logger.warning("Work request failed")
                        _error.value = true
                    }
                    WorkInfo.State.CANCELLED -> {
                        logger.warning("Work request cancelled")
                    }
                    else -> {
                        // Work request is still running or enqueued
                    }
                }
            }
        }
    }

    private fun convertJsonToProducts(json: String?): List<Product> {
        val listType = object : TypeToken<List<Product>>() {}.type
        return Gson().fromJson(json, listType)
    }

//  fetch the project from the api in the case of there is no product we will be fetch from the database
    fun fetchProducts() {
        logger.info("fetch product hit")
        _loading.value = true
        val currentPage = _page.value ?: 1

        viewModelScope.launch {
            try {
                val fetchedProducts = productApi.fetchProducts(currentPage)

                if (!fetchedProducts.isEmpty()){
                    try{
                        insertProduct(fetchedProducts)
                        logger.info("successfully add product to the database")
                        _products.value = fetchedProducts
                    }catch (_e: Exception){
                        logger.warning("fail to add to the database: ${_e}")
                    }
                }else {
                    fetchProductsFromDatabase()
                }
                _loading.value = false
            } catch (e: Exception) {
                logger.warning("Failed to fetch products from API: ${e.message}")
                logger.info("trying to talk to the database")
                fetchProductsFromDatabase()
                _loading.value = false
            }
        }
    }

    // insert the product into database
    fun insertProduct(products: List<Product>) {
        viewModelScope.launch {
            products.forEach { product ->
                repository.insertProduct(product)
            }
        }
    }

    fun setPage(page: Int){
        _page.value = page
    }


    // in case of there is no internet retrieve product from the databse
    private fun fetchProductsFromDatabase() {
        val currentPage = _page.value ?: 1

        viewModelScope.launch {
            try {
                logger.info("start talking to the database")
                val databaseProducts = repository.getProducts(currentPage)
                logger.info("Database fetch size: ${databaseProducts.size}")
                _products.value = databaseProducts
                logger.info("Successfully fetched products from database")
            } catch (e: Exception) {
                logger.warning("Failed to fetch products from database: ${e.message}")
            }
        }
    }
}