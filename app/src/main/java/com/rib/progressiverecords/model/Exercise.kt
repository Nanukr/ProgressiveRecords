package com.rib.progressiverecords.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Exercise(
    @PrimaryKey var exerciseName: String,
    val isDefault: Int = 1,
    val primMuscle: String = "",
    val category: String
)
