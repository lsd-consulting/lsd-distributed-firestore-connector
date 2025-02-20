package io.lsdconsulting.lsd.distributed.firestore.integration.testapp.config

import com.google.api.gax.core.CredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.internal.EmulatorCredentials
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class CredentialsConfig {
    @Bean
    fun credentials(): GoogleCredentials = EmulatorCredentials()

    @Bean
    fun credentialsProvider(credentials: GoogleCredentials) = CredentialsProvider { credentials }
}
