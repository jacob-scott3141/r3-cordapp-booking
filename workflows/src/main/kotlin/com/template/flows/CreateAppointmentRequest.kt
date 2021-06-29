package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.utilities.ProgressTracker
import net.corda.core.flows.FinalityFlow

import net.corda.core.flows.CollectSignaturesFlow

import net.corda.core.transactions.SignedTransaction

import java.util.stream.Collectors

import net.corda.core.flows.FlowSession

import net.corda.core.identity.Party

import com.template.contracts.TemplateContract

import net.corda.core.transactions.TransactionBuilder

import com.template.states.AvailableAppointmentDate
import net.corda.core.contracts.requireThat
import net.corda.core.identity.AbstractParty
import java.util.*


// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class CreateAppointmentRequest(private val doctor: Party,
                                private val alice: Party,
                                private val bob: Party,
                                private val date: Date) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {

        val sender = ourIdentity

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
                .addCommand(TemplateContract.Commands.Create(), listOf(sender.owningKey, alice.owningKey, bob.owningKey))
                .addOutputState(output)

        // Step 4. Verify and sign it with our KeyPair.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)


        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        val otherParties: MutableList<Party> = output.participants.stream().map { el: AbstractParty? -> el as Party? }.collect(Collectors.toList())
        otherParties.remove(ourIdentity)
        val sessions = otherParties.stream().map { el: Party? -> initiateFlow(el!!) }.collect(Collectors.toList())

        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Step 7. Assuming no exceptions, we can now finalise the transaction
        return subFlow<SignedTransaction>(FinalityFlow(stx, sessions))
    }
}

@InitiatedBy(CreateAppointmentRequest::class)
class CreateRequestResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                //Addition checks
                //TODO think about what checks can be done here
            }
        }
        val txId = subFlow(signTransactionFlow).id
        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}