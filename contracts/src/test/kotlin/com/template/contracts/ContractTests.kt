package com.template.contracts

import com.template.states.Appointment
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
    var doctor = TestIdentity(CordaX500Name("Doc", "TestLand", "US"))
    var alice = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    var bob = TestIdentity(CordaX500Name("Bob", "TestLand", "US"))

    @Test
    fun requestTest() {
        val appointmentReferenceState = AvailableAppointmentDate("16-01-2000", doctor.party, alice.party, bob.party)
        val requestState = AppointmentRequest("16-01-2000", doctor.party, alice.party)
        val appointmentState = Appointment("16-01-2000", doctor.party, alice.party)

        ledgerServices.ledger {
            //pass
            transaction {
                //passing transaction
                reference(AppointmentRequestContract.ID, appointmentReferenceState)
                output(AppointmentRequestContract.ID, requestState)
                command(alice.publicKey, AppointmentRequestContract.Commands.Create())
                verifies()
            }
        }
    }
    @Test
    fun acceptTest() {
        val appointmentReferenceState = AvailableAppointmentDate("16-01-2000", doctor.party, alice.party, bob.party)
        val requestState = AppointmentRequest("16-01-2000", doctor.party, alice.party)
        val appointmentState = Appointment("16-01-2000", doctor.party, alice.party)

        ledgerServices.ledger {
            //pass
            transaction {
                //passing transaction
                input(AppointmentRequestContract.ID, requestState)
                input(AppointmentRequestContract.ID, appointmentReferenceState)
                output(AppointmentRequestContract.ID, appointmentState)
                command(doctor.publicKey, AppointmentRequestContract.Commands.Accept())
                verifies()
            }
        }
    }
}