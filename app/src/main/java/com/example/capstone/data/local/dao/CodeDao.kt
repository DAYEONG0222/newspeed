package com.example.capstone.data.local.dao

import androidx.room.*
import com.example.capstone.data.local.entity.Code

@Dao
interface CodeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(codes: List<Code>)

    @Query("SELECT * FROM code")
    suspend fun getAll(): List<Code>
}