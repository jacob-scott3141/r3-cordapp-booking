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

class CreateAppointmentRequestContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.CreateAppointmentRequestContract"
    }

    private fun checkDate(dateStr : String) : Boolean {
        var format = SimpleDateFormat("dd-MM-yyyy", Locale.UK)
        try{
            val date = format.parse(dateStr)
        }
        catch(e : ParseException){
            return false
        }
        return true
    }

    override fun verify(tx: LedgerTransaction) {
        requireThat {
            "No inputs should be consumed when issuing a date" using (tx.inputs.isEmpty())
            "1 reference state should be used" using (tx.referenceStates.size == 1)
            "Only one output state is created" using (tx.outputs.size == 1)

            val out = tx.outputs.single() as AvailableAppointmentDate
            "Dates must be of the format dd-MM-yyyy" using (checkDate(out.date))


        }
    }
    interface Commands : CommandData {
        class Create : CreateAppointmentRequestContract.Commands
    }

}