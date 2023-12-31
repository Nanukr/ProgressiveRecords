package com.rib.progressiverecords.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.rib.progressiverecords.model.Record
import com.rib.progressiverecords.model.Session

data class SessionWithRecords(
    @Embedded val session: Session,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val records: List<Record>
)
