package com.template.contracts

import com.template.states.AvailableAppointmentDate
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.text.ParseException
import java.text.SimpleDateFormat

class AppointmentDateContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.AppointmentDateContract"
    }

    private fun checkDate(dateStr : String) : Boolean {
        var format = SimpleDateFormat("dd-MM-yyyy")
        try{
            format.parse(dateStr)
        }
        catch(e : ParseException){
            return false
        }
        return true
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.findCommand<Commands> { true }
        when (command.value) {
            is Commands.Create -> {
                requireThat {
                    "No inputs should be consumed when issuing a date" using (tx.inputs.isEmpty())
                    "No reference states should be used" using (tx.references.isEmpty())
                    "Only one output state is created" using (tx.outputs.size == 1)

                    val in1 = tx.outputsOfType<AvailableAppointmentDate>()[0]
                    "Date must match format dd-MM-yyyy" using (checkDate(in1.date))

                    val signer = tx.commandsOfType<AppointmentDateContract.Commands.Create>()[0].signers[0]
                    val doctor = in1.doctor

                    "The doctor must be the signer of the transaction" using (signer == doctor.owningKey)
                }
            }

            is Commands.Consume -> {
                requireThat {
                    // do nothing for now
                }
            }
        }
    }
    interface Commands : CommandData {
        class Create : Commands
        class Consume : Commands
    }

}