package io.lsdconsulting.lsd.distributed.firestore.repository

import io.lsdconsulting.lsd.distributed.connector.model.InterceptedFlow
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentAdminRepository

class InterceptedDocumentFirestoreAdminRepository(
) : InterceptedDocumentAdminRepository {
    override fun findRecentFlows(resultSizeLimit: Int): List<InterceptedFlow> {
        TODO("Not yet implemented")
    }
}
