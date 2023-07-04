package com.rib.progressiverecords.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Exercise(
    @PrimaryKey val exerciseName: String
)
