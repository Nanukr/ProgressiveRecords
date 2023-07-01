package com.rib.progressiverecords.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Record(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val exerciseId: Int,
    val repetitions: Int?,
    val weight: Int?,
    val setNumber: Int
)
