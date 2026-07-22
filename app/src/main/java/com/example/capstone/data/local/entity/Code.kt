package com.example.capstone.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "code"
)
data class Code(
    @PrimaryKey
    @ColumnInfo(name = "code")
    val code : String,

    @ColumnInfo(name = "info")
    val info : String
)
