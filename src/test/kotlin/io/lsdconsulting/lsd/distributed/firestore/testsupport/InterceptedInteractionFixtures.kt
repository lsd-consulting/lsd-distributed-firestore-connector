package io.lsdconsulting.lsd.distributed.firestore.testsupport

import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

/**
 * Builds [InterceptedInteraction] with stable defaults for assertions.
 * (k-random 2.2.x is available on JVM 21 for randomized cases.)
 */
fun sampleInterceptedInteraction(
    traceId: String = UUID.randomUUID().toString(),
    body: String? = """{"ok":true}""",
    requestHeaders: Map<String, Collection<String>> = mapOf("content-type" to listOf("application/json")),
    responseHeaders: Map<String, Collection<String>> = mapOf("content-type" to listOf("application/json")),
    serviceName: String = "service",
    target: String = "target",
    path: String = "/path",
    httpStatus: String? = "200",
    httpMethod: String? = "GET",
    interactionType: InteractionType = InteractionType.REQUEST,
    profile: String? = "test",
    elapsedTime: Long = 25L,
    createdAt: ZonedDateTime = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")),
) = InterceptedInteraction(
    traceId = traceId,
    body = body,
    requestHeaders = requestHeaders,
    responseHeaders = responseHeaders,
    serviceName = serviceName,
    target = target,
    path = path,
    httpStatus = httpStatus,
    httpMethod = httpMethod,
    interactionType = interactionType,
    profile = profile,
    elapsedTime = elapsedTime,
    createdAt = createdAt,
)
