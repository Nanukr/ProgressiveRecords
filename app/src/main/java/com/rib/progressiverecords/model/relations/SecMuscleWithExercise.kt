package com.rib.progressiverecords.model.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.rib.progressiverecords.model.Exercise
import com.rib.progressiverecords.model.ExerciseSecMuscleCrossRef
import com.rib.progressiverecords.model.Muscle

data class SecMuscleWithExercise(
    @Embedded val muscle: Muscle,
    @Relation(
        parentColumn = "muscleName",
        entityColumn = "exerciseName",
        associateBy = Junction(ExerciseSecMuscleCrossRef::class)
    )
    val exercises: List<Exercise>
)
