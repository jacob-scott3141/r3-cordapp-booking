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

class AppointmentContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.AcceptAppointmentContract"
    }

    override fun verify(tx: LedgerTransaction) {
        requireThat {
            "2 inputs should be consumed when accepting an appointment" using (tx.inputs.size == 2)
            "Only one output state is created" using (tx.outputs.size == 1)

            //val out = tx.outputs.single() as Appointment


        }
    }
    interface Commands : CommandData {
        class Create : AppointmentContract.Commands
    }

}