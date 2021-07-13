package com.template.states

import com.template.contracts.AppointmentDateContract
import com.template.schemas.AppointmentDateSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.lang.IllegalStateException

// *********
// * State *
// *********
@BelongsToContract(AppointmentDateContract::class)
data class AvailableAppointmentDate(val date: String,
                                    val doctor: Party,
                                    val alice: Party,
                                    val bob: Party,
                                    override val participants: List<AbstractParty> = listOf(alice, bob, doctor)
) : QueryableState {
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is AppointmentDateSchemaV1 -> AppointmentDateSchemaV1.AvailableDate(
                date = this.date
            )
            else -> throw IllegalStateException("Unrecognized schema ${schema.name} passed.")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(AppointmentDateSchemaV1)
    }
}
