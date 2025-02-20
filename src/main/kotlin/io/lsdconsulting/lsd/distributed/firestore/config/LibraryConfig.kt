package io.lsdconsulting.lsd.distributed.firestore.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.FirestoreOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import io.lsdconsulting.lsd.distributed.firestore.repository.InterceptedDocumentFirestoreRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

const val DATABASE_NAME = "interceptedInteractions"

@Configuration
class LibraryConfig {
    @Bean
    fun interceptedDocumentFirestoreRepository(
        collection: CollectionReference,
        @Value("\${lsd.dist.db.maxNumberOfInteractionsToQuery:100}") maxNumberOfInteractionsToQuery: Int,
        @Value("\${lsd.dist.db.timeToLiveDuration:-1d}") timeToLiveDuration: Duration
    ) = InterceptedDocumentFirestoreRepository(collection, maxNumberOfInteractionsToQuery, timeToLiveDuration)

    @Bean
    fun collection(
        @Value("\${lsd.dist.connectionString:(default)}") databaseName: String,
        @Value("\${spring.cloud.gcp.project-id}") projectId: String,
        @Value("\${spring.cloud.gcp.firestore.host-port:#{null}}") hostAndPort: String?,
        credentials: GoogleCredentials
    ): CollectionReference {
        try {
            FirebaseApp.getInstance()
        } catch (ex: IllegalStateException) {
            val firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder().setCredentials(credentials)
                .setDatabaseId(databaseName)
            hostAndPort?.let { firestoreOptions.setEmulatorHost(it) }
            val firebaseOptionsBuilder = FirebaseOptions.builder().setCredentials(credentials).setProjectId(projectId)
            FirebaseApp.initializeApp(firebaseOptionsBuilder.setFirestoreOptions(firestoreOptions.build()).build())
        }
        return FirestoreClient.getFirestore().collection(DATABASE_NAME)
    }
}
