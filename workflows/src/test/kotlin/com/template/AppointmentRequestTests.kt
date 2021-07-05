package com.template

import com.template.flows.CreateAppointmentDate
import com.template.flows.CreateAppointmentRequest
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Future
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import com.template.states.AvailableAppointmentDate
import net.corda.core.node.services.Vault
import net.corda.core.utilities.getOrThrow
import net.corda.testing.common.internal.testNetworkParameters
import java.util.*


class AppointmentRequestTests {
    private lateinit var network: MockNetwork
    private lateinit var doctor: StartedMockNode
    private lateinit var alice: StartedMockNode
    private lateinit var bob: StartedMockNode

    @Before
    fun setup() {
        val myNetworkParameters = testNetworkParameters(minimumPlatformVersion = 4)
        network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
            TestCordapp.findCordapp("com.template.contracts"),
            TestCordapp.findCordapp("com.template.flows")
        ),
        networkParameters = myNetworkParameters))
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
    fun requestAppointmentTest() {
        val availableDateFlow = CreateAppointmentDate(alice.info.legalIdentities[0], bob.info.legalIdentities[0], Date().toString())
        val future1 = doctor.startFlow(availableDateFlow)

        network.runNetwork()
        future1.getOrThrow()

//        val dateAttribute = AppointmentDateSchemaV1.AvailableDate::date.equal(Date().toString())
//        val customCriteria = QueryCriteria.VaultCustomQueryCriteria(dateAttribute)
        val inputCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(Vault.StateStatus.UNCONSUMED)
        val appointmentDate = alice.services.vaultService.queryBy(AvailableAppointmentDate::class.java, inputCriteria).states[0]

        val flow = CreateAppointmentRequest(doctor.info.legalIdentities[0], Date().toString(), appointmentDate)
        val future: Future<SignedTransaction> = alice.startFlow(flow)
        network.runNetwork()
        future.getOrThrow()

    }
}