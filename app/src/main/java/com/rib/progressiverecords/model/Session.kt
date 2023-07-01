package com.rib.progressiverecords.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val sessionName: String?,
    val week: Int,
    val sessionExercises: List<Int>?
)
