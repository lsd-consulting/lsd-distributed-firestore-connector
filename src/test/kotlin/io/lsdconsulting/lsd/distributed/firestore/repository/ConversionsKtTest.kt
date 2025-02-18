package io.lsdconsulting.lsd.distributed.firestore.repository

import com.google.cloud.Timestamp
import io.github.krandom.KRandom
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldMatchAll
import io.kotest.matchers.shouldBe
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

internal class ConversionsKtTest {
    private val kRandom = KRandom()

    @ParameterizedTest
    @MethodSource("provideDurations")
    fun `should convert intercepted interaction to map`(duration: Duration) {
        val interceptedInteraction = kRandom.nextObject(InterceptedInteraction::class.java)

        val map = interceptedInteraction.toMap(duration)

        map.shouldMatchAll(
            TRACE_ID to { it shouldBe interceptedInteraction.traceId },
            BODY to { it shouldBe interceptedInteraction.body },
            REQUEST_HEADERS to {
                it shouldBe interceptedInteraction.requestHeaders.map { entry -> entry.key to entry.value.toList() }
                    .toMap()
            },
            RESPONSE_HEADERS to {
                it shouldBe interceptedInteraction.responseHeaders.map { entry -> entry.key to entry.value.toList() }
                    .toMap()
            },
            SERVICE_NAME to { it shouldBe interceptedInteraction.serviceName },
            TARGET to { it shouldBe interceptedInteraction.target },
            PATH to { it shouldBe interceptedInteraction.path },
            HTTP_STATUS to { it shouldBe interceptedInteraction.httpStatus },
            HTTP_METHOD to { it shouldBe interceptedInteraction.httpMethod },
            INTERACTION_TYPE to { it shouldBe interceptedInteraction.interactionType.name },
            PROFILE to { it shouldBe interceptedInteraction.profile },
            ELAPSED_TIME to { it shouldBe interceptedInteraction.elapsedTime },
            CREATED_AT to {
                it shouldBe Timestamp.ofTimeSecondsAndNanos(
                    interceptedInteraction.createdAt.toEpochSecond(), interceptedInteraction.createdAt.nano
                )
            })

        if (!duration.isZero && !duration.isNegative) {
            val expirationAt = interceptedInteraction.createdAt.plus(duration)
            map.shouldContain(
                EXPIRATION_AT to Timestamp.ofTimeSecondsAndNanos(
                    expirationAt.toEpochSecond(), expirationAt.nano
                )
            )
        }
    }

    @Test
    fun `should convert map to intercepted interaction`() {
        val traceId = randomAlphanumeric(10)
        val body = randomAlphanumeric(10)
        val requestHeaderKey = randomAlphanumeric(5)
        val requestHeaderValue = listOf(randomAlphanumeric(10))
        val responseHeaderKey = randomAlphanumeric(5)
        val responseHeaderValue = listOf(randomAlphanumeric(10))
        val serviceName = randomAlphabetic(10)
        val target = randomAlphabetic(5)
        val path = randomAlphabetic(5)
        val httpStatus = "OK"
        val httpMethod = "POST"
        val profile = randomAlphabetic(5)
        val elapsedTime = kRandom.nextLong()
        val createdAt = Timestamp.of(kRandom.nextObject(Date::class.java))
        val map = mapOf(
            TRACE_ID to traceId,
            BODY to body,
            REQUEST_HEADERS to mapOf(requestHeaderKey to requestHeaderValue),
            RESPONSE_HEADERS to mapOf(responseHeaderKey to responseHeaderValue),
            SERVICE_NAME to serviceName,
            TARGET to target,
            PATH to path,
            HTTP_STATUS to httpStatus,
            HTTP_METHOD to httpMethod,
            INTERACTION_TYPE to InteractionType.CONSUME.name,
            PROFILE to profile,
            ELAPSED_TIME to elapsedTime,
            CREATED_AT to createdAt,
            ZONE to "UTC"
        )

        val interceptedInteraction = map.toInterceptedInteraction()

        interceptedInteraction shouldBeEqual InterceptedInteraction(
            traceId = traceId,
            body = body,
            requestHeaders = mapOf(requestHeaderKey to requestHeaderValue),
            responseHeaders = mapOf(responseHeaderKey to responseHeaderValue),
            serviceName = serviceName,
            target = target,
            path = path,
            httpStatus = httpStatus,
            httpMethod = httpMethod,
            interactionType = InteractionType.CONSUME,
            profile = profile,
            elapsedTime = elapsedTime,
            createdAt = ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(createdAt.seconds, createdAt.nanos.toLong()), ZoneId.of("UTC")
            )
        )
    }

    @Test
    fun `should convert zoned date time to gcp timestamp`() {
        val zonedDateTime = kRandom.nextObject(ZonedDateTime::class.java)

        val timestamp = zonedDateTime.toTimestamp()

        timestamp shouldBe Timestamp.ofTimeSecondsAndNanos(zonedDateTime.toEpochSecond(), zonedDateTime.nano)
    }

    @Test
    fun `should convert gcp timestamp to zoned date time`() {
        val timestamp = Timestamp.of(kRandom.nextObject(Date::class.java))

        val zonedDateTime = timestamp.toZonedDateTime(ZoneId.of("UTC"))

        zonedDateTime shouldBe ZonedDateTime.ofInstant(
            Instant.ofEpochSecond(
                timestamp.seconds, timestamp.nanos.toLong()
            ), ZoneId.of("UTC")
        )
    }

    companion object {
        @JvmStatic
        private fun provideDurations() = listOf(
            Arguments.of(Duration.ofDays(-1L)), Arguments.of(Duration.ZERO), Arguments.of(Duration.ofDays(1L))
        )
    }
}