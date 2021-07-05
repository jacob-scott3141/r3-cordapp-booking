package com.template.contracts

import com.template.states.Appointment
import com.template.states.AppointmentRequest
import com.template.states.AvailableAppointmentDate
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class ContractTests {
    private val ledgerServices: MockServices = MockServices(listOf("com.template"))
    var doctor = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    var alice = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    var bob = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))

    @Test
    fun requestTest() {
        val stateReference = AvailableAppointmentDate("16-01-2000", doctor.party, alice.party, bob.party)
        val state = AppointmentRequest("16-01-2000", doctor.party, alice.party)

        ledgerServices.ledger {
            // Should fail bid price is equal to previous highest bid
            transaction {
                //failing transaction
                output(CreateAppointmentRequestContract.ID, state)
                command(alice.publicKey, CreateAppointmentRequestContract.Commands.Create())
                fails()
            }
            //pass
            transaction {
                //passing transaction
                reference(CreateAppointmentRequestContract.ID, stateReference)
                output(CreateAppointmentRequestContract.ID, state)
                command(alice.publicKey, CreateAppointmentRequestContract.Commands.Create())
                verifies()
            }
        }
    }
}