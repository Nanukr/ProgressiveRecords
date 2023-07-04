package com.rib.progressiverecords.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Record(
    @PrimaryKey val id: UUID,
    val sessionId: UUID,
    val exerciseName: String,
    val repetitions: Int,
    val weight: Int,
    val setNumber: Int
)
