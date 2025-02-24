package io.lsdconsulting.lsd.distributed.firestore.integration

import com.google.cloud.Timestamp
import com.google.cloud.firestore.CollectionReference
import io.github.krandom.KRandom
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.REQUEST
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.firestore.integration.testapp.TestApplication
import io.lsdconsulting.lsd.distributed.firestore.repository.*
import org.apache.commons.lang3.RandomStringUtils.secure
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
import java.time.temporal.ChronoUnit.MILLIS

@Suppress("UNCHECKED_CAST")
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
    fun `should save and retrieve from database`() {
        val interceptedInteraction = InterceptedInteraction(
            elapsedTime = 20L,
            httpStatus = "OK",
            serviceName = "service",
            target = "target",
            path = "/path",
            httpMethod = "GET",
            body = "body",
            interactionType = REQUEST,
            traceId = secure().nextAlphanumeric(6),
            createdAt = ZonedDateTime.now(ZoneId.of("UTC")).truncatedTo(MILLIS)
        )

        underTest.save(interceptedInteraction)

        val result = underTest.findByTraceIds(interceptedInteraction.traceId)
        result shouldHaveSize 1
        result[0].elapsedTime shouldBe 20L
        result[0].httpStatus shouldBe "OK"
        result[0].path shouldBe "/path"
        result[0].httpMethod shouldBe "GET"
        result[0].body shouldBe "body"
        result[0].interactionType shouldBe REQUEST
        result[0].createdAt shouldBe interceptedInteraction.createdAt
    }

    @Test
    fun `records should have a ttl and sanitize header keys`() {
        val traceId = secure().nextAlphanumeric(10)
        val createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        val interceptedInteraction =
            kRandom.nextObject(InterceptedInteraction::class.java).copy(
                traceId = traceId,
                createdAt = createdAt,
                requestHeaders = mapOf(
                    "good1" to listOf(secure().nextAlphanumeric(10)),
                    "__bad1__" to listOf(secure().nextAlphanumeric(10))
                ),
                responseHeaders = mapOf(
                    "good2" to listOf(secure().nextAlphanumeric(10)),
                    "__bad2__" to listOf(secure().nextAlphanumeric(10))
                )
            )

        underTest.save(interceptedInteraction)

        val documents = collection.whereEqualTo("traceId", traceId).get().get().documents
        documents shouldHaveSize 1
        val data = documents[0].data
        data shouldContainKey EXPIRATION_AT
        data[EXPIRATION_AT] as Timestamp shouldBeGreaterThan createdAt.toTimestamp()
        data shouldContainKey REQUEST_HEADERS
        data[REQUEST_HEADERS] as Map<String, List<String>> shouldContainKey "good1"
        data[REQUEST_HEADERS] as Map<String, List<String>> shouldContainKey "_bad1_"
        data[RESPONSE_HEADERS] as Map<String, List<String>> shouldContainKey "good2"
        data[RESPONSE_HEADERS] as Map<String, List<String>> shouldContainKey "_bad2_"
    }

    @Test
    fun `should save and retrieve in correct order`() {
        val traceId = secure().nextAlphanumeric(10)
        val interceptedInteractions = (1..10)
            .map { _ ->
                kRandom.nextObject(InterceptedInteraction::class.java).copy(
                    traceId = traceId,
                    createdAt = ZonedDateTime.now(ZoneId.of("UTC")).truncatedTo(MILLIS),
                )
            }
            .sortedByDescending { it.createdAt }

        interceptedInteractions.forEach { underTest.save(it) }

        val result = underTest.findByTraceIds(traceId)
        result shouldHaveSize 10
        (1..10).forEach { result[it - 1].createdAt shouldBe (interceptedInteractions[10 - it].createdAt) }
    }

    companion object {
        private const val FIRESTORE_EMULATOR_IMAGE = "gcr.io/google.com/cloudsdktool/google-cloud-cli:emulators"

        @Container
        private val firestoreContainer: FirestoreEmulatorContainer = FirestoreEmulatorContainer(
            DockerImageName.parse(FIRESTORE_EMULATOR_IMAGE)
        )

        @Suppress("unused")
        @DynamicPropertySource
        @JvmStatic
        fun emulatorProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.cloud.gcp.firestore.host-port", firestoreContainer::getEmulatorEndpoint)
        }
    }
}
