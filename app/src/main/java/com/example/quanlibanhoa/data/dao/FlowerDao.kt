package com.example.quanlibanhoa.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.quanlibanhoa.data.entity.Flower

@Dao
interface FlowerDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(flower: Flower): Long

    @Transaction
    @Update
    suspend fun update(flower: Flower)

    @Transaction
    @Delete
    suspend fun delete(flower: Flower)

    @Transaction
    @Delete
    suspend fun deleteMultiple(flowers: List<Flower>)

    @Query("SELECT * FROM flowers ORDER BY tenHoa ASC")
    fun getAllFlowers(): LiveData<List<Flower>>

    @Query("SELECT * FROM flowers WHERE id = :id LIMIT 1")
    suspend fun getFlowerById(id: Int): Flower?
}
