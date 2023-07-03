package com.rib.progressiverecords.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.Record

data class ExerciseWithRecords(
    @Embedded val exercise: Exercise,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseId"
    )
    val records: List<Record>
)
