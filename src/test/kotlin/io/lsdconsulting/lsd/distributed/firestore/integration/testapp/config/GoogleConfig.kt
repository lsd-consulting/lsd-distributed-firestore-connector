package io.lsdconsulting.lsd.distributed.firestore.integration.testapp.config

import com.google.api.gax.core.CredentialsProvider
import com.google.auth.Credentials
import com.google.cloud.firestore.FirestoreOptions.EmulatorCredentials
import com.google.cloud.spring.core.GcpProjectIdProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class GoogleConfig {
    @Bean
    fun projectIdProvider(
        @Value("\${spring.cloud.gcp.project-id}") projectId: String
    ) = GcpProjectIdProvider { projectId }

    @Bean
    fun credentials(): Credentials = EmulatorCredentials()

    @Bean
    fun credentialsProvider(credentials: Credentials): CredentialsProvider = CredentialsProvider { credentials }
}
