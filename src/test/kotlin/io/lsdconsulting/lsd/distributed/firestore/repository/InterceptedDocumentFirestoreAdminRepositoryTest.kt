package io.lsdconsulting.lsd.distributed.firestore.repository

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class InterceptedDocumentFirestoreAdminRepositoryTest {
    private val underTest = InterceptedDocumentFirestoreAdminRepository()

    @Test
    fun `should throw not implemented exception on find recent flows`() {
        assertThrows<NotImplementedError> {
            underTest.findRecentFlows(1)
        }
    }
}