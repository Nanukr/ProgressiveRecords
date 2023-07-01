package com.rib.progressiverecords.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Program(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val programName: String?,
    val programSessions: List<Int>?,
    val weekSpan: Int?
)
