package com.template

import com.template.flows.CreateAppointmentDate
import com.template.states.AvailableAppointmentDate
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Future;
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.node.services.Vault.StateStatus
import net.corda.core.utilities.getOrThrow
import java.util.*


class FlowTests {
    private lateinit var network: MockNetwork
    private lateinit var doctor: StartedMockNode
    private lateinit var alice: StartedMockNode
    private lateinit var bob: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
            TestCordapp.findCordapp("com.template.contracts"),
            TestCordapp.findCordapp("com.template.flows")
        )))
        doctor = network.createPartyNode()
        alice = network.createPartyNode()
        bob = network.createPartyNode()
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }
    @Test
    fun createAvailableDateTest() {
        val flow = CreateAppointmentDate(alice.info.legalIdentities[0], bob.info.legalIdentities[0], Date().toString())
        val future: Future<SignedTransaction> = doctor.startFlow(flow)
        network.runNetwork()

        //successful query means the state is stored at node alice's vault. Flow went through.
        val inputCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(StateStatus.UNCONSUMED)
        val state = alice.services.vaultService.queryBy(AvailableAppointmentDate::class.java, inputCriteria).states[0].state.data

        future.getOrThrow()
        /*
        TODO sort this out
            O=Mock Company 3, L=London, C=GB has finished prematurely and we're trying to send them the finalised transaction.
            Did they forget to call ReceiveFinalityFlow? (Tried to access ended session SessionId(toLong=4361128774027148188))
            net.corda.core.flows.UnexpectedFlowEndException: O=Mock Company 3, L=London, C=GB has finished prematurely and
            we're trying to send them the finalised transaction. Did they forget to call ReceiveFinalityFlow?
         */
    }
}