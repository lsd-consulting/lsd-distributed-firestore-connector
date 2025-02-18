package io.lsdconsulting.lsd.distributed.firestore.repository

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.google.api.core.SettableApiFuture
import com.google.cloud.firestore.*
import io.github.krandom.KRandom
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutionException

private const val MAX_NUMBER_OF_INTERACTIONS_TO_QUERY = 10

internal class InterceptedDocumentFirestoreRepositoryTest {
    private val collection = mockk<CollectionReference>(relaxed = true)
    private val timeToLiveDuration = Duration.ofDays(1L)
    private val underTest = InterceptedDocumentFirestoreRepository(
        collection, MAX_NUMBER_OF_INTERACTIONS_TO_QUERY, timeToLiveDuration
    )
    private val kRandom = KRandom()
    private val mockkDocumentReference = mockk<DocumentReference>(relaxed = true)

    @Test
    fun `should not throw execution exception on save`() {
        val interceptedInteraction = kRandom.nextObject(InterceptedInteraction::class.java)
        val uuid = UUID.randomUUID()
        mockkStatic(UUID::randomUUID)
        every { UUID.randomUUID() } returns uuid
        every { collection.document(eq(uuid.toString())) } returns mockkDocumentReference
        val listAppender = setUpTestLogger()

        shouldNotThrow<ExecutionException> { underTest.save(interceptedInteraction) }

        verify { collection.document(eq(uuid.toString())) }
        verify { mockkDocumentReference.set(any()) }
        listAppender.list shouldHaveSize 0
    }

    @Test
    fun `should log when execution exception is thrown`() {
        val interceptedInteraction = kRandom.nextObject(InterceptedInteraction::class.java)
        val uuid = UUID.randomUUID()
        mockkStatic(UUID::randomUUID)
        every { UUID.randomUUID() } returns uuid
        every { collection.document(eq(uuid.toString())) } returns mockkDocumentReference
        every { mockkDocumentReference.set(any()) } returns SettableApiFuture.create<WriteResult?>()
            .apply { setException(RuntimeException("Error")) }
        val listAppender = setUpTestLogger()

        shouldNotThrow<ExecutionException> { underTest.save(interceptedInteraction) }

        verify { collection.document(eq(uuid.toString())) }
        verify { mockkDocumentReference.set(any()) }
        val iLoggingEvents = listAppender.list
        iLoggingEvents[0].level shouldBe Level.ERROR
        iLoggingEvents[0].message shouldMatch "Skipping persisting the interceptedInteraction due to exception - interceptedInteraction:.*".toRegex()
    }

    @Test
    fun `should return list of intercepted interactions on find by trace ids`() {
        val mockkTraceIdQuery = mockk<Query>()
        val mockLimitQuery = mockk<Query>()
        val mockOrderByCreatedAtQuery = mockk<Query>()
        every { collection.whereIn("traceId", any()) } returns mockkTraceIdQuery
        every { mockkTraceIdQuery.limit(eq(MAX_NUMBER_OF_INTERACTIONS_TO_QUERY)) } returns mockLimitQuery
        every { mockLimitQuery.orderBy(eq("createdAt")) } returns mockOrderByCreatedAtQuery
        val mockkQuerySnapshot = mockk<QuerySnapshot>()
        every { mockOrderByCreatedAtQuery.get() } returns SettableApiFuture.create<QuerySnapshot?>().apply {
            set(mockkQuerySnapshot)
        }
        val mockkQueryDocumentSnapshot = mockk<QueryDocumentSnapshot>()
        every { mockkQuerySnapshot.documents } returns listOf(mockkQueryDocumentSnapshot)
        val interceptedInteraction = kRandom.nextObject(InterceptedInteraction::class.java)
        every { mockkQueryDocumentSnapshot.data } returns interceptedInteraction.toMap(timeToLiveDuration)
        val listAppender = setUpTestLogger()

        val interceptedInteractionList = underTest.findByTraceIds(UUID.randomUUID().toString())

        listAppender.list shouldHaveSize 0
        interceptedInteractionList shouldContainExactly listOf(interceptedInteraction)
    }

    @Test
    fun `should log error if execution exception is thrown on find by trace ids`() {
        val mockkTraceIdQuery = mockk<Query>()
        val mockLimitQuery = mockk<Query>()
        val mockOrderByCreatedAtQuery = mockk<Query>()
        every { collection.whereIn("traceId", any()) } returns mockkTraceIdQuery
        every { mockkTraceIdQuery.limit(eq(MAX_NUMBER_OF_INTERACTIONS_TO_QUERY)) } returns mockLimitQuery
        every { mockLimitQuery.orderBy(eq("createdAt")) } returns mockOrderByCreatedAtQuery
        every { mockOrderByCreatedAtQuery.get() } returns SettableApiFuture.create<QuerySnapshot?>().apply {
            setException(RuntimeException("Error"))
        }
        val listAppender = setUpTestLogger()

        val interceptedInteractionList = underTest.findByTraceIds(UUID.randomUUID().toString())

        val iLoggingEvents = listAppender.list
        iLoggingEvents shouldHaveSize 1
        iLoggingEvents[0].level shouldBe Level.ERROR
        iLoggingEvents[0].message shouldMatch "Failed to retrieve interceptedInteractions - message:.*".toRegex()
        interceptedInteractionList shouldHaveSize 0
    }

    @Test
    fun `should return true on is active method`() {
        underTest.isActive() shouldBe true
    }

    companion object {
        @JvmStatic
        private fun setUpTestLogger(): ListAppender<ILoggingEvent> {
            // get Logback Logger
            val fooLogger: Logger =
                LoggerFactory.getLogger(InterceptedDocumentFirestoreRepository::class.java) as Logger
            // create and start a ListAppender
            val listAppender = ListAppender<ILoggingEvent>()
            listAppender.start()
            // add the appender to the logger
            fooLogger.addAppender(listAppender)
            return listAppender
        }
    }
}
