package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.AppointmentContract
import com.template.contracts.AppointmentRequestContract
import com.template.states.Appointment
import com.template.states.AppointmentRequest
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.node.services.StatesNotAvailableException
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.stream.Collectors


// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class DenyAppointmentRequest(private val alice: Party,
                             private val request: StateAndRef<AppointmentRequest>) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {

        val doctor = ourIdentity

        // Step 1. Get a reference to the notary service on our network and our key pair.
        // Note: ongoing work to support multiple notary identities is still in progress.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        //Compose the State that carries the appointment information


        // Step 3. Create a new TransactionBuilder object. The request is going to be consumed, but the availableDate will
        //         continue to be available
        val builder = TransactionBuilder(notary)
                        .addInputState(request)
                        .addCommand(AppointmentRequestContract.Commands.Deny(), listOf(doctor.owningKey))


        // Step 4. Verify and sign it with our KeyPair.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        val otherParties: MutableList<Party> = mutableListOf(alice)
        val sessions = otherParties.stream().map { el: Party? -> initiateFlow(el!!) }.collect(Collectors.toList())

        // Step 7. Assuming no exceptions, we can now finalise the transaction
        return subFlow(FinalityFlow(ptx, sessions))
    }

}

@InitiatedBy(DenyAppointmentRequest::class)
class DenialResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}
