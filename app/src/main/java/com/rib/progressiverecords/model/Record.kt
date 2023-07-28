package com.rib.progressiverecords.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Record(
    @PrimaryKey val id: UUID,
    val sessionId: UUID,
    val exerciseName: String,
    var repetitions: Int,
    var weight: Int,
    val setNumber: Int
)
