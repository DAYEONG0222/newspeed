package com.example.capstone.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "user"
)
data class User (
    @PrimaryKey
    @ColumnInfo(name = "name")
    val name : String,

    @ColumnInfo(name = "birth_date")
    val birthDate : LocalDate,
    @ColumnInfo(name = "join_date")
    val joinDate : LocalDate
)