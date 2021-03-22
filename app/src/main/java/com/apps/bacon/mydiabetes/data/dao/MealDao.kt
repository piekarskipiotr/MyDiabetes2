package com.apps.bacon.mydiabetes.data.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.apps.bacon.mydiabetes.data.entities.Meal

@Dao
interface MealDao {
    @Query("SELECT EXISTS(SELECT * FROM meals WHERE :name == meal_name)")
    fun checkForMealExist(name: String): Boolean

    @Query("SELECT * FROM meals ORDER BY meal_name ASC")
    fun getAll(): LiveData<List<Meal>>

    @Query("SELECT * FROM meals WHERE is_editable == 1 ORDER BY meal_name ASC")
    fun getAllLocal(): LiveData<List<Meal>>

    @Query("SELECT * FROM meals ORDER BY meal_name ASC")
    fun getAllPaging(): DataSource.Factory<Int, Meal>

    @Query("SELECT * FROM meals WHERE :id == meal_id")
    fun getMeal(id: Int): Meal

    @Query("SELECT MAX(meal_id) FROM meals")
    fun getLastId(): Int

    @Insert
    suspend fun insert(meal: Meal)

    @Update
    suspend fun update(meal: Meal)

    @Delete
    suspend fun delete(meal: Meal)
}