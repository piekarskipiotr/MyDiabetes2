package com.apps.bacon.mydiabetes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_server")
data class MealServer(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "meal_id")
    var id: Int,

    @ColumnInfo(name = "meal_name")
    var name: String,

    @ColumnInfo(name = "calories")
    var calories: Double,

    @ColumnInfo(name = "carbohydrateExchangers")
    var carbohydrateExchangers: Double,

    @ColumnInfo(name = "proteinFatExchangers")
    var proteinFatExchangers: Double,

    @ColumnInfo(name = "icon")
    var icon: String?
)