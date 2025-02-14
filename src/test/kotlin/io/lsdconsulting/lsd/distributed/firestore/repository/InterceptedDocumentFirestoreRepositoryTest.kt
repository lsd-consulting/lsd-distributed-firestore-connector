package io.lsdconsulting.lsd.distributed.firestore.repository

import io.github.krandom.KRandom
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class InterceptedDocumentFirestoreRepositoryTest {
    private val underTest = InterceptedDocumentFirestoreRepository()
    private val kRandom = KRandom()

    @Test
    fun `should throw not implemented exception on save`() {
        val interceptedInteraction = kRandom.nextObject(InterceptedInteraction::class.java)

        assertThrows<NotImplementedError> {
            underTest.save(interceptedInteraction)
        }
    }

    @Test
    fun `should throw not implemented exception on find by trace ids`() {
        assertThrows<NotImplementedError> {
            underTest.findByTraceIds("")
        }
    }

    @Test
    fun `should throw not implemented exception on is active`() {
        assertThrows<NotImplementedError> {
            underTest.isActive()
        }
    }
}