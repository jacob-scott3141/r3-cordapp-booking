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
//TODO is this the right way to make the contract visible to anyone? Like, is the constructor right?
data class AvailableAppointmentDate(val date: String,
                                    val doctor: Party,
                                    val alice: Party,
                                    val bob: Party,
                                    override val participants: List<AbstractParty> = listOf(doctor, bob, alice)
) : ContractState
