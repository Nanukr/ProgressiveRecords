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

    @TypeConverter
    fun fromTimeLength(time: TimeLength?): Int? {
        var seconds: Int? = null
        if (time != null) { seconds =  time.hours * 3600 + time.minutes * 60 + time.seconds }
        return seconds
    }

    @TypeConverter
    fun toTimeLength(seconds: Int?): TimeLength? {
        val hours: Int
        val remainingSecondsAfterHours: Int

        val minutes: Int
        val remainingSeconds: Int

        val timeLength: TimeLength?

        if (seconds != null) {
            hours = seconds / 3600
            remainingSecondsAfterHours = seconds % 3600

            minutes = remainingSecondsAfterHours / 60
            remainingSeconds = remainingSecondsAfterHours % 60

            timeLength = TimeLength(
                seconds = remainingSeconds,
                minutes = minutes,
                hours = hours
            )
        } else {
            timeLength = null
        }

        return timeLength
    }
}