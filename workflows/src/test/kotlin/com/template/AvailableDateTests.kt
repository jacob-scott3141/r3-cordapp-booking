package com.template

import com.natpryce.hamkrest.assertion.assertThat
import com.template.flows.CreateAppointmentDate
import com.template.states.AvailableAppointmentDate
import net.corda.core.identity.Party
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
import kotlin.test.assertEquals


class AvailableDateTests {
    private lateinit var network: MockNetwork
    private lateinit var doctor: StartedMockNode
    private lateinit var alice: StartedMockNode
    private lateinit var bob: StartedMockNode
    private lateinit var patientList: List<Party>

    @Before
    fun setup() {
        network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
            TestCordapp.findCordapp("com.template.contracts"),
            TestCordapp.findCordapp("com.template.flows")
        )))
        doctor = network.createPartyNode()
        alice = network.createPartyNode()
        bob = network.createPartyNode()
        patientList = listOf(bob.info.legalIdentities[0], alice.info.legalIdentities[0])
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }
    @Test
    fun createAvailableDateTest() {
        val flow = CreateAppointmentDate(patientList, "06-07-2021")
        val future: Future<SignedTransaction> = doctor.startFlow(flow)
        network.runNetwork()

        //successful query means the state is stored at node alice's vault. Flow went through.
        val inputCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(StateStatus.UNCONSUMED)
        val state = alice.services.vaultService.queryBy(AvailableAppointmentDate::class.java, inputCriteria).states

        assertEquals(1, state.size, "date not created")

        future.getOrThrow()
    }
}
