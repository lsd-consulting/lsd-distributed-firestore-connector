package io.lsdconsulting.lsd.distributed.firestore.repository

import com.google.cloud.firestore.CollectionReference
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import lsd.logging.log
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutionException

class InterceptedDocumentFirestoreRepository(
    private val collection: CollectionReference,
    private val maxNumberOfInteractionsToQuery: Int,
    private val timeToLiveDuration: Duration
) : InterceptedDocumentRepository {
    override fun save(interceptedInteraction: InterceptedInteraction) {
        if (isActive()) {
            try {
                val id = UUID.randomUUID().toString() // create an id for the new record
                collection.document(id).set(interceptedInteraction.toMap(timeToLiveDuration)).get()
            } catch (e: ExecutionException) {
                log().error(
                    "Skipping persisting the interceptedInteraction due to exception - interceptedInteraction:{}, message:{}, stackTrace:{}",
                    interceptedInteraction,
                    e.message,
                    e.stackTrace
                )
            }
        }
    }

    override fun findByTraceIds(vararg traceId: String): List<InterceptedInteraction> {
        if (isActive()) {
            try {
                return collection.whereIn("traceId", traceId.toList()).limit(maxNumberOfInteractionsToQuery)
                    .orderBy("createdAt").get().get().documents.map {
                        it.data.toInterceptedInteraction()
                    }
            } catch (e: ExecutionException) {
                log().error("Failed to retrieve interceptedInteractions - message:${e.message}", e.stackTrace)
            }
        }
        return emptyList()
    }

    override fun isActive() = true
}
