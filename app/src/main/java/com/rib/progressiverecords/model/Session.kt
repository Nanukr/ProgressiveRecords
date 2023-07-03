package com.rib.progressiverecords.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Session(
    @PrimaryKey val id: UUID,
    val sessionName: String,
    val recordId: UUID,
    val date: Date
)
