package com.rib.progressiverecords.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Session(
    @PrimaryKey val id: UUID,
    val sessionName: String,
    val date: Date,
    @ColumnInfo(name = "isTemplate", defaultValue = "0")
    val isTemplate: Int = 0
)
