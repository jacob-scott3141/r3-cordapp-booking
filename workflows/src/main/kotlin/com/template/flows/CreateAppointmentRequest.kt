package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.AppointmentRequestContract
import net.corda.core.flows.*
import net.corda.core.utilities.ProgressTracker
import net.corda.core.flows.FinalityFlow

import net.corda.core.transactions.SignedTransaction

import java.util.stream.Collectors

import net.corda.core.flows.FlowSession

import net.corda.core.identity.Party

import com.template.states.AppointmentRequest

import net.corda.core.transactions.TransactionBuilder

import com.template.states.AvailableAppointmentDate
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.StatesNotAvailableException

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class CreateAppointmentRequest(private val doctor: Party,
                               private val date: String) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        lateinit var appointmentDate: StateAndRef<AvailableAppointmentDate>

        val vaultAppointmentDates = serviceHub.vaultService.queryBy(AvailableAppointmentDate::class.java).states

        var found : Boolean = false
        for(stateAndRef in vaultAppointmentDates){
            if(stateAndRef.state.data.date == this.date){
                appointmentDate = stateAndRef
                found = true
            }
        }

        if(found) {
            val patient = ourIdentity

            // Step 1. Get a reference to the notary service on our network and our key pair.
            // Note: ongoing work to support multiple notary identities is still in progress.
            val notary = serviceHub.networkMapCache.notaryIdentities[0]

            //Compose the State that carries the appointment information
            val output = AppointmentRequest(
                date,
                doctor,
                patient
            )

            // Step 3. Create a new TransactionBuilder object.
            val builder = TransactionBuilder(notary)
                .addCommand(AppointmentRequestContract.Commands.Create(), listOf(patient.owningKey))
                .addOutputState(output)
                .addReferenceState(appointmentDate.referenced())

            // Step 4. Verify and sign it with our KeyPair.
            builder.verify(serviceHub)
            val ptx = serviceHub.signInitialTransaction(builder)


            // Step 6. Collect the other party's signature using the SignTransactionFlow.
            val otherParties: MutableList<Party> = mutableListOf(doctor)
            val sessions = otherParties.stream().map { el: Party? -> initiateFlow(el!!) }.collect(Collectors.toList())

            // Step 7. Assuming no exceptions, we can now finalise the transaction
            return subFlow(FinalityFlow(ptx, sessions))
        }
        else{
            throw StatesNotAvailableException("State with date %s not found".format(date))
        }
    }
}

@InitiatedBy(CreateAppointmentRequest::class)
class CreateRequestResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        return subFlow(ReceiveFinalityFlow(counterpartySession));
    }
}
