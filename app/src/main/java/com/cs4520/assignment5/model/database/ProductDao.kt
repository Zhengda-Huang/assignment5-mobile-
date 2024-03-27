package com.cs4520.assignment5.model.database

import androidx.room.*

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(product: Product)

    @Insert
    suspend fun insertAll(products: List<Product>)

    @Query("SELECT * FROM product_table LIMIT :pageSize OFFSET :offset")
    suspend fun getProductsByPage(pageSize: Int, offset: Int): List<Product>

}