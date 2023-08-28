package com.rib.progressiverecords.model.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.ExerciseSecMuscleCrossRef
import com.rib.progressiverecords.model.Muscle

data class ExerciseWithSecMuscle(
    @Embedded val exercise: Exercise,
    @Relation(
        parentColumn = "exerciseName",
        entityColumn = "muscleName",
        associateBy = Junction(ExerciseSecMuscleCrossRef::class)
    )
    val muscles: List<Muscle>
)