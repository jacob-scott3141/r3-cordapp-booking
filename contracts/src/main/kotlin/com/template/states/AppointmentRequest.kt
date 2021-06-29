package com.template.states

import com.template.contracts.TemplateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(TemplateContract::class)
data class AppointmentRequest(val date: Date,
                              val doctor: Party,
                              val patient: Party,
                              override val participants: List<AbstractParty> = listOf(doctor, patient)
) : ContractState