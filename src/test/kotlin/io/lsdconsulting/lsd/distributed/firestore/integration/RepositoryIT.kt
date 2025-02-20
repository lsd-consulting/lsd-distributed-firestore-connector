package io.lsdconsulting.lsd.distributed.firestore.integration

import com.google.cloud.Timestamp
import com.google.cloud.firestore.CollectionReference
import io.github.krandom.KRandom
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.maps.shouldContainKey
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.firestore.integration.testapp.TestApplication
import io.lsdconsulting.lsd.distributed.firestore.repository.EXPIRATION_AT
import io.lsdconsulting.lsd.distributed.firestore.repository.InterceptedDocumentFirestoreRepository
import io.lsdconsulting.lsd.distributed.firestore.repository.toTimestamp
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.FirestoreEmulatorContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.ZoneId
import java.time.ZonedDateTime

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [TestApplication::class])
@Testcontainers
@ActiveProfiles("test")
internal class RepositoryIT {
    protected val kRandom = KRandom()

    @Autowired
    protected lateinit var collection: CollectionReference

    @Autowired
    protected lateinit var underTest: InterceptedDocumentFirestoreRepository

    @Test
    fun `records should have a ttl`() {
        val traceId = randomAlphanumeric(10)
        val createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        val interceptedInteraction =
            kRandom.nextObject(InterceptedInteraction::class.java).copy(traceId = traceId, createdAt = createdAt)

        underTest.save(interceptedInteraction)

        val documents = collection.whereEqualTo("traceId", traceId).get().get().documents
        documents shouldHaveSize 1
        documents[0].data.shouldContainKey(EXPIRATION_AT)
        documents[0].data[EXPIRATION_AT] as Timestamp shouldBeGreaterThan createdAt.toTimestamp()
    }

    companion object {
        private const val FIRESTORE_EMULATOR_IMAGE = "gcr.io/google.com/cloudsdktool/google-cloud-cli:emulators"

        @Container
        private val firestoreContainer: FirestoreEmulatorContainer = FirestoreEmulatorContainer(
            DockerImageName.parse(FIRESTORE_EMULATOR_IMAGE)
        )

        @DynamicPropertySource
        @JvmStatic
        fun emulatorProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.cloud.gcp.firestore.host-port", firestoreContainer::getEmulatorEndpoint)
        }
    }
}
