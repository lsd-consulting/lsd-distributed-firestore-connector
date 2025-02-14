package io.lsdconsulting.lsd.distributed.firestore.repository

import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository

class InterceptedDocumentFirestoreRepository : InterceptedDocumentRepository {
    override fun save(interceptedInteraction: InterceptedInteraction) {
        TODO()
    }

    override fun findByTraceIds(vararg traceId: String): List<InterceptedInteraction> {
        TODO()
    }

    override fun isActive(): Boolean {
        TODO()
    }
}
