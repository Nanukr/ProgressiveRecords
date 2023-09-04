package com.rib.progressiverecords.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Exercise(
    @PrimaryKey var exerciseName: String,

    @ColumnInfo(name = "isDefault", defaultValue = "1")
    val isDefault: Int = 1,

    @ColumnInfo(name = "primMuscle", defaultValue = "")
    val primMuscle: String,

    @ColumnInfo(name = "category", defaultValue = "")
    val category: String
)
