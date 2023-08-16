package com.rib.progressiverecords.model

import androidx.room.Entity

@Entity(primaryKeys = ["exerciseName", "muscleName"])
data class ExerciseSecMuscleCrossRef(
    val exerciseName: String,
    val muscleName: String
)
