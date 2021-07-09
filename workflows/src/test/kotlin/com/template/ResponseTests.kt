package com.template

import com.template.flows.ApproveAppointmentRequest
import com.template.flows.CreateAppointmentDate
import com.template.flows.CreateAppointmentRequest
import com.template.flows.DenyAppointmentRequest
import com.template.schemas.AppointmentDateSchemaV1
import com.template.states.AppointmentRequest
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Future
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import com.template.states.AvailableAppointmentDate
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.utilities.getOrThrow
import net.corda.testing.common.internal.testNetworkParameters
import java.util.*
import kotlin.test.assertEquals


class ResponseTests {
    private lateinit var network: MockNetwork
    private lateinit var doctor: StartedMockNode
    private lateinit var alice: StartedMockNode
    private lateinit var bob: StartedMockNode
    private lateinit var patientList: List<Party>

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
        patientList = listOf(bob.info.legalIdentities[0], alice.info.legalIdentities[0])
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }
    @Test
    fun approveRequest() {
        val availableDateFlow = CreateAppointmentDate(patientList, "06-07-2021")
        val future1 = doctor.startFlow(availableDateFlow)

        network.runNetwork()
        future1.getOrThrow()

        val dateAttribute = AppointmentDateSchemaV1.AvailableDate::date.equal("06-07-2021")
        val customCriteria = QueryCriteria.VaultCustomQueryCriteria(dateAttribute)
        val appointmentDate = alice.services.vaultService.queryBy(AvailableAppointmentDate::class.java, customCriteria).states[0]

        // should try to run a vault query to see if the appointment request state was in fact created
        val flow = CreateAppointmentRequest(doctor.info.legalIdentities[0], "06-07-2021", appointmentDate)
        val future: Future<SignedTransaction> = alice.startFlow(flow)
        network.runNetwork()
        val inputCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(Vault.StateStatus.UNCONSUMED)
        val appointmentRequest = alice.services.vaultService.queryBy(AppointmentRequest::class.java, inputCriteria).states[0]
        future.getOrThrow()

        val approvalFlow = ApproveAppointmentRequest(alice.info.legalIdentities[0],
            mutableListOf(bob.info.legalIdentities[0]),
            "06-07-2021",
            appointmentDate,
            appointmentRequest)
        val approvalFuture = doctor.startFlow(approvalFlow)
        network.runNetwork()
        approvalFuture.getOrThrow()

        val consumedCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(Vault.StateStatus.CONSUMED)
        val consumedDate = alice.services.vaultService.queryBy(AppointmentRequest::class.java, consumedCriteria).states
        val consumedReq = alice.services.vaultService.queryBy(AppointmentRequest::class.java, consumedCriteria).states

        assertEquals(1, consumedDate.size, "date not consumed")
        assertEquals(1, consumedReq.size, "request not consumed")

    }

    @Test
    fun denyRequest() {
        val availableDateFlow = CreateAppointmentDate(patientList, "06-07-2021")
        val future1 = doctor.startFlow(availableDateFlow)

        network.runNetwork()
        future1.getOrThrow()

        val dateAttribute = AppointmentDateSchemaV1.AvailableDate::date.equal("06-07-2021")
        val customCriteria = QueryCriteria.VaultCustomQueryCriteria(dateAttribute)
        val appointmentDate = alice.services.vaultService.queryBy(AvailableAppointmentDate::class.java, customCriteria).states[0]

        // should try to run a vault query to see if the appointment request state was in fact created
        val flow = CreateAppointmentRequest(doctor.info.legalIdentities[0], "06-07-2021", appointmentDate)
        val future: Future<SignedTransaction> = alice.startFlow(flow)
        network.runNetwork()
        val inputCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(Vault.StateStatus.UNCONSUMED)
        val appointmentRequest = alice.services.vaultService.queryBy(AppointmentRequest::class.java, inputCriteria).states[0]
        future.getOrThrow()

        val denyingFlow = DenyAppointmentRequest(alice.info.legalIdentities[0],
            appointmentRequest)
        val denyingFuture = doctor.startFlow(denyingFlow)
        network.runNetwork()
        denyingFuture.getOrThrow()

        val consumedCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(Vault.StateStatus.CONSUMED)
        val unconsumedDate = alice.services.vaultService.queryBy(AvailableAppointmentDate::class.java, inputCriteria).states
        val consumedReq = alice.services.vaultService.queryBy(AppointmentRequest::class.java, consumedCriteria).states

        assertEquals(1, unconsumedDate.size, "date consumed")
        assertEquals(1, consumedReq.size, "request not consumed")

    }
}
