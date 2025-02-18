package io.lsdconsulting.lsd.distributed.firestore.repository

import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

internal class InterceptedDocumentFirestoreAdminRepositoryTest {
    private val underTest = InterceptedDocumentFirestoreAdminRepository()

    @Test
    fun `should throw not implemented exception on find recent flows`() {
        shouldThrow<NotImplementedError> {
            underTest.findRecentFlows(1)
        }
    }
}
