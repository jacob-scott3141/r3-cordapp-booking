package com.template.contracts

import com.template.states.Appointment
import com.template.states.AvailableAppointmentDate
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DenyAppointmentContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.DenyAppointmentContract"
    }

    override fun verify(tx: LedgerTransaction) {
        requireThat {
            "1 input should be consumed when denying an appointment" using (tx.inputs.size == 1)
            "No output state is created" using (tx.outputs.isEmpty())

            //val out = tx.outputs.single() as AvailableAppointmentDate


        }
    }

    interface Commands : CommandData {
        class Create : TemplateContract.Commands
    }

}