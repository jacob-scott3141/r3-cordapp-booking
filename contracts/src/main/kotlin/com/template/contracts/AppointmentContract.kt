package com.template.contracts

import com.template.states.Appointment
import com.template.states.AppointmentRequest
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
            "2 inputs should be consumed when accepting a request" using (tx.inputs.size == 2)
            "No reference states should be used when accepting a request" using (tx.references.isEmpty())
            "1 output state is created" using (tx.outputs.size == 1)

            val in1 = tx.inputsOfType<AppointmentRequest>()[0]

            val signer = tx.commandsOfType<AppointmentContract.Commands.Create>()[0].signers[0]
            val doctor = in1.doctor

            "The doctor must be the first signer of the transaction" using (signer == doctor.owningKey)
        }
    }
    interface Commands : CommandData {
        class Create : AppointmentContract.Commands
    }

}