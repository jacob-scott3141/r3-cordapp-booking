package com.template.contracts

import com.template.states.AppointmentRequest
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.text.ParseException
import java.text.SimpleDateFormat

class AppointmentRequestContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.AppointmentRequestContract"
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
                    "1 reference state should be used" using (tx.references.size == 1)
                    "Only one output state is created" using (tx.outputs.size == 1)

                    val out = tx.outputStates[0] as AppointmentRequest
                    "Dates must be of the format dd-MM-yyyy" using (checkDate(out.date))
                }
            }
            is Commands.Accept -> {
                requireThat {
                    "2 inputs should be consumed when accepting a request" using (tx.inputs.size == 1)
                    "1 Output state is created" using (tx.outputs.size == 1)
                }
            }
            is Commands.Deny -> {
                requireThat {
                    "1 input should be consumed when denying a request" using (tx.inputs.size == 1)
                    "No output states are created" using (tx.outputs.isEmpty())
                }
            }
        }

    }
    interface Commands : CommandData {
        class Create : TypeOnlyCommandData(), Commands
        class Accept : TypeOnlyCommandData(), Commands
        class Deny : TypeOnlyCommandData(), Commands
    }
}