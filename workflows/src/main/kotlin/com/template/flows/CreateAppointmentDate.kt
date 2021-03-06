package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.AppointmentDateContract
import net.corda.core.flows.*
import net.corda.core.utilities.ProgressTracker
import net.corda.core.flows.FinalityFlow

import net.corda.core.transactions.SignedTransaction

import java.util.stream.Collectors

import net.corda.core.flows.FlowSession

import net.corda.core.identity.Party

import net.corda.core.transactions.TransactionBuilder

import com.template.states.AvailableAppointmentDate
import net.corda.core.identity.AbstractParty


// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class CreateAppointmentDate(private val alice: Party,
                            private val bob: Party,
                            private val date: String) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {

        val doctor = ourIdentity

        // Step 1. Get a reference to the notary service on our network and our key pair.
        // Note: ongoing work to support multiple notary identities is still in progress.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        //Compose the State that carries the appointment information
        val output = AvailableAppointmentDate(
            date,
            doctor,
            alice,
            bob
        )

        // Step 3. Create a new TransactionBuilder object.
        val builder = TransactionBuilder(notary)
                .addCommand(AppointmentDateContract.Commands.Create(), listOf(doctor.owningKey))
                .addOutputState(output)

        // Step 4. Verify and sign it with our KeyPair.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)


        // Step 6. Collect the other party's and send using the FinalityFlow.
        val otherParties: MutableList<Party> = listOf(alice, bob) as MutableList<Party>
        val sessions = otherParties.stream().map { el: Party? -> initiateFlow(el!!) }.collect(Collectors.toList())

        // Step 7. Assuming no exceptions, we can now finalise the transaction
        return subFlow(FinalityFlow(ptx, sessions))
    }
}

@InitiatedBy(CreateAppointmentDate::class)
class CreateDateResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}
