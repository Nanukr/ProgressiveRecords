package com.rib.progressiverecords.database

import androidx.room.TypeConverter
import com.rib.progressiverecords.model.TimeLength
import java.time.Duration
import java.util.*

class RecordTypeConverters {
    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch: Long): Date {
        return Date(millisSinceEpoch)
    }
}