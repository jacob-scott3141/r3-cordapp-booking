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
import net.corda.core.node.services.StatesNotAvailableException
import sun.security.ec.point.ProjectivePoint
import java.util.*


// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class ApproveAppointmentRequest(private val alice: Party,
                                private val date: String) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        lateinit var appointmentDate: StateAndRef<AvailableAppointmentDate>
        lateinit var aliceNotify: Party
        lateinit var bobNotify: Party

        val vaultAppointmentDates = serviceHub.vaultService.queryBy(AvailableAppointmentDate::class.java).states

        var found1 : Boolean = false
        for(stateAndRef in vaultAppointmentDates){
            if(stateAndRef.state.data.date == this.date){
                appointmentDate = stateAndRef

                aliceNotify = stateAndRef.state.data.alice
                bobNotify = stateAndRef.state.data.bob
                found1 = true
            }
        }

        lateinit var appointmentRequest: StateAndRef<AppointmentRequest>

        val vaultAppointmentRequests = serviceHub.vaultService.queryBy(AppointmentRequest::class.java).states

        var found2 : Boolean = false
        for(stateAndRef in vaultAppointmentRequests){
            if(stateAndRef.state.data.date == this.date && stateAndRef.state.data.patient == this.alice){
                appointmentRequest = stateAndRef
                found2 = true
            }
        }

        if(found1 && found2) {
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
                .addInputState(appointmentDate)
                .addCommand(AppointmentDateContract.Commands.Consume(), listOf(doctor.owningKey))
                .addInputState(appointmentRequest)
                .addCommand(AppointmentRequestContract.Commands.Accept(), listOf(doctor.owningKey))
                .addOutputState(output)

            // Step 4. Verify and sign it with our KeyPair.
            builder.verify(serviceHub)
            val ptx = serviceHub.signInitialTransaction(builder)


            // Step 6. Collect the other party's signature using the SignTransactionFlow.
            val otherParties: MutableList<Party> = listOf(aliceNotify, bobNotify) as MutableList<Party>
            val sessions = otherParties.stream().map { el: Party? -> initiateFlow(el!!) }.collect(Collectors.toList())

            // Step 7. Assuming no exceptions, we can now finalise the transaction
            return subFlow(FinalityFlow(ptx, sessions))
        }
        else{
            throw StatesNotAvailableException("State with date %s and patient %s not found".format(date, alice.name.commonName))
        }
    }
}

@InitiatedBy(ApproveAppointmentRequest::class)
class ApprovalResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}
