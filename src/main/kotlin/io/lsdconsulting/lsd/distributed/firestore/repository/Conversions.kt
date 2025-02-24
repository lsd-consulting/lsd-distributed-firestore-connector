package io.lsdconsulting.lsd.distributed.firestore.repository

import com.google.cloud.Timestamp
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

internal const val TRACE_ID = "traceId"
internal const val BODY = "body"
internal const val REQUEST_HEADERS = "requestHeaders"
internal const val RESPONSE_HEADERS = "responseHeaders"
internal const val SERVICE_NAME = "serviceName"
internal const val TARGET = "target"
internal const val PATH = "path"
internal const val HTTP_STATUS = "httpStatus"
internal const val HTTP_METHOD = "httpMethod"
internal const val INTERACTION_TYPE = "interactionType"
internal const val PROFILE = "profile"
internal const val ELAPSED_TIME = "elapsedTime"
internal const val CREATED_AT = "createdAt"
internal const val EXPIRATION_AT = "expirationAt"
internal const val ZONE = "zone"
internal const val FORBIDDEN_REGEX = "__.*__"
internal const val SANITIZED_REGEX = "_.*_"

internal fun InterceptedInteraction.toMap(timeToLive: Duration) = mutableMapOf(
    TRACE_ID to traceId,
    BODY to body,
    REQUEST_HEADERS to requestHeaders.map { it.key.sanitize() to it.value.toList() }.toMap(),
    RESPONSE_HEADERS to responseHeaders.map { it.key.sanitize() to it.value.toList() }.toMap(),
    SERVICE_NAME to serviceName,
    TARGET to target,
    PATH to path,
    HTTP_STATUS to httpStatus,
    HTTP_METHOD to httpMethod,
    INTERACTION_TYPE to interactionType.name,
    PROFILE to profile,
    ELAPSED_TIME to elapsedTime,
    CREATED_AT to createdAt.toTimestamp(),
    ZONE to createdAt.zone.toString()
) + if (timeToLive.isNegative || timeToLive.isZero) {
    emptyMap()
} else {
    mapOf(EXPIRATION_AT to createdAt.plus(timeToLive).toTimestamp())
}

@Suppress("UNCHECKED_CAST")
internal fun Map<String, Any?>.toInterceptedInteraction() = InterceptedInteraction(
    traceId = this[TRACE_ID] as String,
    body = this[BODY] as String?,
    requestHeaders = (this[REQUEST_HEADERS] as Map<String, Collection<String>>).map { it.key.expand() to it.value }
        .toMap(),
    responseHeaders = (this[RESPONSE_HEADERS] as Map<String, Collection<String>>).map { it.key.expand() to it.value }
        .toMap(),
    serviceName = this[SERVICE_NAME] as String,
    target = this[TARGET] as String,
    path = this[PATH] as String,
    httpStatus = this[HTTP_STATUS] as String?,
    httpMethod = this[HTTP_METHOD] as String?,
    interactionType = InteractionType.valueOf(this[INTERACTION_TYPE] as String),
    profile = this[PROFILE] as String?,
    elapsedTime = this[ELAPSED_TIME] as Long,
    createdAt = (this[CREATED_AT] as Timestamp).toZonedDateTime(ZoneId.of(this[ZONE] as String))
)

internal fun String.sanitize(): String =
    if (this.matches(FORBIDDEN_REGEX.toRegex())) this.drop(1).dropLast(1) else this

internal fun String.expand(): String =
    if (this.matches(SANITIZED_REGEX.toRegex())) "_${this}_" else this

internal fun ZonedDateTime.toTimestamp() = Timestamp.ofTimeSecondsAndNanos(this.toEpochSecond(), this.nano)

internal fun Timestamp.toZonedDateTime(zoneId: ZoneId) =
    ZonedDateTime.ofInstant(Instant.ofEpochSecond(this.seconds, this.nanos.toLong()), zoneId)
