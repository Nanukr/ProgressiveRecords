package com.rib.progressiverecords.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Muscle (
    @PrimaryKey val muscleName: String
)