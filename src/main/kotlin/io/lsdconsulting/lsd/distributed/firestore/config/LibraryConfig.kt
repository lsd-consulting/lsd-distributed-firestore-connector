package io.lsdconsulting.lsd.distributed.firestore.config

import com.google.api.gax.core.CredentialsProvider
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.FirestoreOptions
import com.google.cloud.spring.core.GcpProjectIdProvider
import io.lsdconsulting.lsd.distributed.firestore.repository.InterceptedDocumentFirestoreAdminRepository
import io.lsdconsulting.lsd.distributed.firestore.repository.InterceptedDocumentFirestoreRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

const val DATABASE_NAME = "interceptedInteractions"

@Configuration
class LibraryConfig {
    @Bean
    fun interceptedDocumentFirestoreAdminRepository() = InterceptedDocumentFirestoreAdminRepository()

    @Bean
    fun interceptedDocumentFirestoreRepository(
        collection: CollectionReference,
        @Value("\${lsd.dist.db.maxNumberOfInteractionsToQuery:100}") maxNumberOfInteractionsToQuery: Int,
        @Value("\${lsd.dist.db.timeToLiveDuration:-1d}") timeToLiveDuration: Duration
    ) = InterceptedDocumentFirestoreRepository(collection, maxNumberOfInteractionsToQuery, timeToLiveDuration)

    @Bean
    fun collection(
        @Value("\${lsd.dist.connectionString:(default)}") databaseName: String,
        @Value("\${spring.cloud.gcp.firestore.host-port:#{null}}") hostAndPort: String?,
        projectIdProvider: GcpProjectIdProvider,
        credentialsProvider: CredentialsProvider
    ): CollectionReference {
        val firestoreOptionsBuilder =
            FirestoreOptions
                .getDefaultInstance()
                .toBuilder()
                .setProjectId(projectIdProvider.projectId)
                .setCredentialsProvider(credentialsProvider)
                .setDatabaseId(databaseName)
        hostAndPort?.let { firestoreOptionsBuilder.setEmulatorHost(it) }
        return firestoreOptionsBuilder.build().service.collection(DATABASE_NAME)
    }
}
