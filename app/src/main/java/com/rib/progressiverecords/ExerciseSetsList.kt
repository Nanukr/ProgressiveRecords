package com.rib.progressiverecords

import com.rib.progressiverecords.model.Record

data class ExerciseSetsList(
    val totalSets: List<List<Record>> = emptyList()
) {
    fun organizeRecords(records: List<Record>): ExerciseSetsList {

        val totalSets: MutableList<List<Record>> = mutableListOf()
        val currentExerciseSets: MutableList<Record> = mutableListOf()
        var currentExercise = ""
        records.forEach { record ->
            if (record.exerciseName == currentExercise) {
                currentExerciseSets.add(record)
            } else {
                if (currentExercise != "") {
                    totalSets.add(currentExerciseSets.toList())
                    currentExerciseSets.clear()
                }
                currentExerciseSets.add(record)
            }
            currentExercise = record.exerciseName
        }
        totalSets.add(currentExerciseSets.toList())
        return copy(totalSets = totalSets.toList())
    }
}