package com.template

import com.template.flows.CreateAppointmentDate
import com.template.flows.CreateDateResponder
import net.corda.core.identity.CordaX500Name
import net.corda.testing.internal.chooseIdentityAndCert
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test


class MockNetworkTests {
    private lateinit var mockNet: MockNetwork
    private lateinit var nodeD: StartedMockNode
    private lateinit var nodeA: StartedMockNode
    private lateinit var nodeB: StartedMockNode

    @Before
    fun setUp() {
        mockNet = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows")),
                notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary","London","GB")
                ))))

        nodeD = mockNet.createNode(CordaX500Name("Doctor", "London", "GB"))
        nodeA = mockNet.createNode(CordaX500Name("Alice", "London", "GB"))
        nodeB = mockNet.createNode(CordaX500Name("Bob", "London", "GB"))

        val startedNodes = arrayListOf(nodeD,nodeA,nodeB)
        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach { it.registerInitiatedFlow(CreateDateResponder::class.java) }
        mockNet.runNetwork()
    }

    @Test
    fun flowReturnsCorrectState() {
        val doc = nodeD.info.chooseIdentityAndCert().party
        val alice = nodeA.info.chooseIdentityAndCert().party
        val bob = nodeB.info.chooseIdentityAndCert().party

        println("found parties")

        val flow = CreateAppointmentDate(alice,bob,"16-01-2000")
        val future = nodeD.startFlow(flow)
        mockNet.runNetwork()

        println("date correct")
    }

    @After
    fun cleanUp() {
        mockNet.stopNodes()
    }
}