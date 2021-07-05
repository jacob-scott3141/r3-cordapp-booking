package com.template.contracts

import com.template.states.AppointmentRequest
import com.template.states.AvailableAppointmentDate
import net.corda.core.identity.CordaX500Name
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class ContractTests {
    private val ledgerServices: MockServices = MockServices(listOf("com.template"),
        TestIdentity(CordaX500Name("Test","Test","US")),
        networkParameters = testNetworkParameters(minimumPlatformVersion = 4))
    var doctor = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    var alice = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    var bob = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))

    @Test
    fun requestTest() {
        val stateReference = AvailableAppointmentDate("16-01-2000", doctor.party, alice.party, bob.party)
        val state = AppointmentRequest("16-01-2000", doctor.party, alice.party)

        ledgerServices.ledger {
            //pass
            transaction {
                //passing transaction
                reference(AppointmentRequestContract.ID, stateReference)
                output(AppointmentRequestContract.ID, state)
                command(alice.publicKey, AppointmentRequestContract.Commands.Create())
                verifies()
            }
        }
    }
}