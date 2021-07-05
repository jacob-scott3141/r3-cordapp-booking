package com.template.schemas

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Index
import javax.persistence.Table

object AppointmentDateSchema

object AppointmentDateSchemaV1 : MappedSchema(
    schemaFamily = AppointmentDateSchema.javaClass,
    version = 1,
    mappedTypes = listOf(AvailableDate::class.java)
) {
    @Entity
    @Table(
        name = "state_dates", indexes = [
            Index(name = "date_idx", columnList = "date")
        ]
    )
    class AvailableDate(
        @Column(name = "date", nullable = false)
        var date: String? = null
    ) : PersistentState()
}