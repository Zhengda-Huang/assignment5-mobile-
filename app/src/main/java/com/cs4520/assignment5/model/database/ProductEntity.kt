package com.cs4520.assignment5.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_table")
data class Product(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "type")val type: String,
    @ColumnInfo(name = "expiryDate")val expiryDate: String?,
    @ColumnInfo(name = "price")val price: Double
)