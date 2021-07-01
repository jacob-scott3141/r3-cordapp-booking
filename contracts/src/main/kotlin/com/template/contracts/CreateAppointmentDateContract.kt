package com.template.contracts

import com.template.states.Appointment
import com.template.states.AvailableAppointmentDate
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CreateAppointmentDateContract : Contract {

    override fun verify(tx: LedgerTransaction) {
        requireThat {
            "No inputs should be consumed when issuing a date" using (tx.inputs.isEmpty())
            "Only one output state is created" using (tx.outputs.size == 1)

            //functions
            val out = tx.outputs.single() as AvailableAppointmentDate
            var bool = true
            var format = SimpleDateFormat("dd-MM-yyyy", Locale.UK)
            try{
                val date = format.parse(out.date)
            }
            catch(e : ParseException){
                bool = false
            }
            "Dates must be of the format dd-MM-yyyy" using (bool)


        }
    }

}