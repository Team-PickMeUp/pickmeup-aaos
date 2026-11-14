package com.example.pickmeup.ui.location

import com.example.pickmeup.data.pickup.FakePickupRepository
import com.example.pickmeup.data.pickup.PickupRepository
import com.example.pickmeup.model.LocationShareResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class LocationShareRequesterTest {

    /** 취소 API 호출 횟수를 세는 저장소 */
    private class CountingRepository(private val delegate: PickupRepository) : PickupRepository by delegate {
        var cancelCount = 0
            private set

        override suspend fun cancelLocationRequest(): Result<Unit> {
            cancelCount++
            return delegate.cancelLocationRequest()
        }
    }

    private fun TestScope.requester(
        responseDelay: Duration = Duration.ZERO,
        deny: Boolean = false,
    ): Pair<LocationShareRequester, CountingRepository> {
        val repository = CountingRepository(FakePickupRepository(responseDelay, denyRequests = deny))
        val scope = TestScope(UnconfinedTestDispatcher(testScheduler))
        return LocationShareRequester(scope, repository) to repository
    }

    private fun TestScope.collectEvents(requester: LocationShareRequester): List<LocationShareEvent> {
        val events = mutableListOf<LocationShareEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { requester.events.collect { events += it } }
        return events
    }

    private val StateFlow<LocationShareRequestState>.remaining: Int
        get() = (value as LocationShareRequestState.Waiting).remainingSeconds

    @Test
    fun waiting_startsAtFiveMinutes_andCountsDownEverySecond() = runTest {
        val (requester, _) = requester(responseDelay = 10.minutes)

        requester.request("홍길동", "01012345678")
        assertEquals(300, requester.state.remaining)

        advanceTimeBy(1.seconds)
        runCurrent()
        assertEquals(299, requester.state.remaining)

        advanceTimeBy(60.seconds)
        runCurrent()
        assertEquals(239, requester.state.remaining)
        requester.cancel()
    }

    @Test
    fun allowed_runsOnAllowedThenSendsEvent_andClosesPopup() = runTest {
        val (requester, repository) = requester()
        val events = collectEvents(requester)
        var saved = false

        requester.request("홍길동", "01012345678") { saved = true }

        assertTrue(saved)
        assertEquals(listOf(LocationShareEvent.Allowed), events)
        assertEquals(LocationShareRequestState.Idle, requester.state.value)
        assertEquals("홍길동", repository.currentShare.value?.targetName)
    }

    @Test
    fun noResponseForFiveMinutes_timesOut_andCancelsOnServer() = runTest {
        val (requester, repository) = requester(responseDelay = 10.minutes)
        val events = collectEvents(requester)

        requester.request("홍길동", "01012345678")
        advanceTimeBy(5.minutes - 1.seconds)
        runCurrent()
        assertEquals(1, requester.state.remaining)

        advanceTimeBy(1.seconds)
        runCurrent()

        assertEquals(LocationShareRequestState.TimedOut, requester.state.value)
        assertEquals(1, repository.cancelCount) // 대상자 링크 무효화
        assertTrue(events.isEmpty())
        assertNull(repository.currentShare.value)
    }

    @Test
    fun denied_showsDeniedPopup_withoutStartingPickup() = runTest {
        val (requester, repository) = requester(deny = true)
        val events = collectEvents(requester)

        requester.request("홍길동", "01012345678")

        assertEquals(LocationShareRequestState.Denied, requester.state.value)
        assertTrue(events.isEmpty())
        assertNull(repository.currentShare.value) // 거부는 진행 중인 픽업이 아님
    }

    @Test
    fun dismissResult_closesPopup_andAllowsNewRequest() = runTest {
        val (requester, _) = requester(deny = true)

        requester.request("홍길동", "01012345678")
        requester.dismissResult()
        assertEquals(LocationShareRequestState.Idle, requester.state.value)

        requester.request("홍길동", "01012345678")
        assertEquals(LocationShareRequestState.Denied, requester.state.value)
    }

    @Test
    fun whilePopupShown_newRequestIsIgnored() = runTest {
        val (requester, _) = requester(responseDelay = 10.minutes)

        requester.request("홍길동", "01012345678")
        advanceTimeBy(10.seconds)
        runCurrent()
        requester.request("김민수", "01098765432")

        // 카운트다운이 처음부터 다시 시작되지 않음
        assertEquals(290, requester.state.remaining)
        requester.cancel()
    }

    @Test
    fun cancel_closesPopup_andCancelsOnServer() = runTest {
        val (requester, repository) = requester(responseDelay = 10.minutes)

        requester.request("홍길동", "01012345678")
        requester.cancel()
        advanceTimeBy(10.minutes)
        runCurrent()

        assertEquals(LocationShareRequestState.Idle, requester.state.value)
        assertEquals(1, repository.cancelCount)
        assertNull(repository.currentShare.value)
    }

    @Test
    fun formatRemaining_padsMinutesAndSeconds() {
        assertEquals("05:00", formatRemaining(300))
        assertEquals("04:59", formatRemaining(299))
        assertEquals("00:09", formatRemaining(9))
        assertEquals("00:00", formatRemaining(-1))
    }

    @Test
    fun deniedResult_isNotKeptAsCurrentShare() = runTest {
        val repository = FakePickupRepository(Duration.ZERO, denyRequests = true)

        val result = repository.requestLocationShare("홍길동", "01012345678")

        assertTrue(result.getOrNull() is LocationShareResult.Denied)
        assertNull(repository.currentShare.value)
    }
}
