package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.AppointmentContract
import com.template.contracts.AppointmentDateContract
import com.template.contracts.AppointmentRequestContract
import net.corda.core.flows.*
import net.corda.core.utilities.ProgressTracker
import net.corda.core.flows.FinalityFlow

import net.corda.core.flows.CollectSignaturesFlow

import net.corda.core.transactions.SignedTransaction

import java.util.stream.Collectors

import net.corda.core.flows.FlowSession

import net.corda.core.identity.Party

import com.template.states.Appointment
import com.template.states.AppointmentRequest
import com.template.states.AvailableAppointmentDate
import net.corda.core.contracts.StateAndRef

import net.corda.core.transactions.TransactionBuilder

import net.corda.core.contracts.requireThat
import net.corda.core.identity.AbstractParty
import java.util.*


// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class ApproveAppointmentRequest(private val alice: Party,
                                private val bob: Party,
                                private val date: String,
                                private val availableDate: StateAndRef<AvailableAppointmentDate>,
                                private val request: StateAndRef<AppointmentRequest>) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {

        val doctor = ourIdentity

        // Step 1. Get a reference to the notary service on our network and our key pair.
        // Note: ongoing work to support multiple notary identities is still in progress.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        //Compose the State that carries the appointment information
        val output = Appointment(
                date,
                doctor,
                alice
        )

        // Step 3. Create a new TransactionBuilder object. The availableDate and the request are going to be consumed.
        val builder = TransactionBuilder(notary)
                .addCommand(AppointmentContract.Commands.Create(), listOf(doctor.owningKey))
                .addInputState(availableDate).addCommand(AppointmentDateContract.Commands.Consume(), listOf(doctor.owningKey))
                .addInputState(request).addCommand(AppointmentRequestContract.Commands.Accept(), listOf(doctor.owningKey))
                .addOutputState(output)

        // Step 4. Verify and sign it with our KeyPair.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)


        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        val otherParties: MutableList<Party> = output.participants.stream().map { el: AbstractParty? -> el as Party? }.collect(Collectors.toList())
        otherParties.add(bob)
        otherParties.remove(ourIdentity)
        val sessions = otherParties.stream().map { el: Party? -> initiateFlow(el!!) }.collect(Collectors.toList())

        // Step 7. Assuming no exceptions, we can now finalise the transaction
        return subFlow<SignedTransaction>(FinalityFlow(ptx, sessions))
    }
}

@InitiatedBy(ApproveAppointmentRequest::class)
class ApprovalResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}


