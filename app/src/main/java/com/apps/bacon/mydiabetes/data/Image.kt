package com.apps.bacon.mydiabetes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class Image(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "image_id")
    var id: Int,

    @ColumnInfo(name = "product_id")
    var productId: Int,

    @ColumnInfo(name = "image_string")
    var image: String

)
