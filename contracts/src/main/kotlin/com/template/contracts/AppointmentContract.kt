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
            "No inputs should be consumed when accepting a request" using (tx.inputs.isEmpty())
            "No reference states should be used when accepting a request" using (tx.references.isEmpty())
            "No Output states are created" using (tx.outputs.isEmpty())
        }
    }
    interface Commands : CommandData {
        class Create : AppointmentContract.Commands
    }

}