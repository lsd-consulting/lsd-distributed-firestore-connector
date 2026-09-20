package io.lsdconsulting.lsd.distributed.firestore.repository

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.google.api.core.SettableApiFuture
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.QueryDocumentSnapshot
import com.google.cloud.firestore.QuerySnapshot
import com.google.cloud.firestore.WriteResult
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.lsdconsulting.lsd.distributed.firestore.testsupport.sampleInterceptedInteraction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ExecutionException

private const val MAX_NUMBER_OF_INTERACTIONS_TO_QUERY = 10

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
internal class InterceptedDocumentFirestoreRepositoryTest {

    @Mock lateinit var collection: CollectionReference
    @Mock lateinit var documentReference: DocumentReference

    private val timeToLiveDuration = Duration.ofDays(1L)
    private lateinit var underTest: InterceptedDocumentFirestoreRepository
    private var uuidStatic: MockedStatic<UUID>? = null

    @BeforeEach
    fun setUp() {
        underTest = InterceptedDocumentFirestoreRepository(
            collection, MAX_NUMBER_OF_INTERACTIONS_TO_QUERY, timeToLiveDuration
        )
    }

    @AfterEach
    fun tearDown() {
        uuidStatic?.close()
        uuidStatic = null
    }

    @Test
    fun `should not throw execution exception on save`() {
        val interceptedInteraction = sampleInterceptedInteraction()
        val uuid = UUID.fromString("11111111-1111-1111-1111-111111111111")
        uuidStatic = Mockito.mockStatic(UUID::class.java)
        uuidStatic!!.`when`<UUID> { UUID.randomUUID() }.thenReturn(uuid)
        whenever(collection.document(uuid.toString())).thenReturn(documentReference)
        whenever(documentReference.set(any())).thenReturn(
            SettableApiFuture.create<WriteResult?>().apply { set(null) }
        )
        val listAppender = setUpTestLogger()

        shouldNotThrow<ExecutionException> { underTest.save(interceptedInteraction) }

        verify(collection).document(uuid.toString())
        verify(documentReference).set(any())
        listAppender.list shouldHaveSize 0
    }

    @Test
    fun `should log when execution exception is thrown`() {
        val interceptedInteraction = sampleInterceptedInteraction()
        val uuid = UUID.fromString("22222222-2222-2222-2222-222222222222")
        uuidStatic = Mockito.mockStatic(UUID::class.java)
        uuidStatic!!.`when`<UUID> { UUID.randomUUID() }.thenReturn(uuid)
        whenever(collection.document(uuid.toString())).thenReturn(documentReference)
        whenever(documentReference.set(any())).thenReturn(
            SettableApiFuture.create<WriteResult?>().apply { setException(RuntimeException("Error")) }
        )
        val listAppender = setUpTestLogger()

        shouldNotThrow<ExecutionException> { underTest.save(interceptedInteraction) }

        verify(collection).document(uuid.toString())
        verify(documentReference).set(any())
        listAppender.list[0].level shouldBe Level.ERROR
        listAppender.list[0].formattedMessage shouldMatch
            ".*Skipping persisting the interceptedInteraction due to exception.*".toRegex()
    }

    @Test
    fun `should return list of intercepted interactions on find by trace ids`() {
        val traceIdQuery = Mockito.mock(Query::class.java)
        val limitQuery = Mockito.mock(Query::class.java)
        val orderByQuery = Mockito.mock(Query::class.java)
        val querySnapshot = Mockito.mock(QuerySnapshot::class.java)
        val queryDocumentSnapshot = Mockito.mock(QueryDocumentSnapshot::class.java)

        whenever(collection.whereIn(eq("traceId"), any())).thenReturn(traceIdQuery)
        whenever(traceIdQuery.limit(MAX_NUMBER_OF_INTERACTIONS_TO_QUERY)).thenReturn(limitQuery)
        whenever(limitQuery.orderBy("createdAt")).thenReturn(orderByQuery)
        whenever(orderByQuery.get()).thenReturn(
            SettableApiFuture.create<QuerySnapshot?>().apply { set(querySnapshot) }
        )
        whenever(querySnapshot.documents).thenReturn(listOf(queryDocumentSnapshot))
        val interceptedInteraction = sampleInterceptedInteraction()
        whenever(queryDocumentSnapshot.data).thenReturn(interceptedInteraction.toMap(timeToLiveDuration))
        val listAppender = setUpTestLogger()

        val result = underTest.findByTraceIds(UUID.randomUUID().toString())

        listAppender.list shouldHaveSize 0
        result shouldContainExactly listOf(interceptedInteraction)
    }

    @Test
    fun `should log error if execution exception is thrown on find by trace ids`() {
        val traceIdQuery = Mockito.mock(Query::class.java)
        val limitQuery = Mockito.mock(Query::class.java)
        val orderByQuery = Mockito.mock(Query::class.java)

        whenever(collection.whereIn(eq("traceId"), any())).thenReturn(traceIdQuery)
        whenever(traceIdQuery.limit(MAX_NUMBER_OF_INTERACTIONS_TO_QUERY)).thenReturn(limitQuery)
        whenever(limitQuery.orderBy("createdAt")).thenReturn(orderByQuery)
        whenever(orderByQuery.get()).thenReturn(
            SettableApiFuture.create<QuerySnapshot?>().apply { setException(RuntimeException("Error")) }
        )
        val listAppender = setUpTestLogger()

        val result = underTest.findByTraceIds(UUID.randomUUID().toString())

        listAppender.list shouldHaveSize 1
        listAppender.list[0].level shouldBe Level.ERROR
        listAppender.list[0].formattedMessage shouldMatch
            ".*Failed to retrieve interceptedInteractions.*".toRegex()
        result shouldHaveSize 0
    }

    @Test
    fun `should return true on is active method`() {
        underTest.isActive() shouldBe true
    }

    private fun setUpTestLogger(): ListAppender<ILoggingEvent> {
        val logger = LoggerFactory.getLogger(InterceptedDocumentFirestoreRepository::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>()
        listAppender.start()
        logger.addAppender(listAppender)
        return listAppender
    }
}
