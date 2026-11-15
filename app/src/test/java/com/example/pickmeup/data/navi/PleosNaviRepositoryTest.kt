package com.example.pickmeup.data.navi

import com.example.pickmeup.model.LocationShareResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class PleosNaviRepositoryTest {

    /** 요청마다 정해 둔 이벤트를 바로 돌려주는 가짜 SDK. null 이면 응답 없음 */
    private class ScriptedNaviClient : NaviClient {
        private val _events = MutableSharedFlow<NaviEvent>(extraBufferCapacity = 16)
        override val events: SharedFlow<NaviEvent> = _events

        var connectResult: Result<Unit>? = Result.success(Unit)
        var isGuiding: Boolean? = true
        var waypointCount: Int? = 0
        var routeResponse: NaviEvent? = NaviEvent.RoutingCompleted
        var waypointResponse: NaviEvent? = NaviEvent.WaypointAdded

        val calls = mutableListOf<String>()
        var connectCount = 0

        override fun connect(onConnected: () -> Unit, onFailed: (Throwable) -> Unit) {
            connectCount++
            connectResult?.fold({ onConnected() }, onFailed)
        }

        override fun requestRoute(location: LocationShareResult.Allowed) {
            calls += "route"
            routeResponse?.let { _events.tryEmit(it) }
        }

        override fun addWaypoint(location: LocationShareResult.Allowed, index: Int) {
            calls += "waypoint:$index"
            waypointResponse?.let { _events.tryEmit(it) }
        }

        override fun cancelRoute() {
            calls += "cancel"
        }

        override fun requestRouteState() {
            isGuiding?.let { _events.tryEmit(NaviEvent.RouteState(isGuiding = it)) }
        }

        override fun requestWaypoints() {
            waypointCount?.let { _events.tryEmit(NaviEvent.Waypoints(it)) }
        }

        fun send(event: NaviEvent) = _events.tryEmit(event)
    }

    private val client = ScriptedNaviClient()
    private val repository = PleosNaviRepository(
        client = client,
        connectTimeout = 10.seconds,
        queryTimeout = 5.seconds,
        completionTimeout = 10.seconds,
        simulatedArrival = null,
    )
    private val location = LocationShareResult.Allowed("홍길동", "서울시 강남구", 37.48, 127.03)

    @Test
    fun destination_requestsRoute_andSucceedsOnRoutingCompleted() = runTest {
        val result = repository.setDestination(location)

        assertTrue(result.isSuccess)
        assertEquals(listOf("route"), client.calls)
    }

    @Test
    fun destination_succeedsOnRouteStarted_likeRealNavi() = runTest {
        // Pleos 내비 3.0.4 실기기: onRoutingCompleted 없이 onRouteStarted 만 옴
        client.routeResponse = NaviEvent.RouteStarted

        assertTrue(repository.setDestination(location).isSuccess)
    }

    @Test
    fun destination_naviError_fails() = runTest {
        client.routeResponse = NaviEvent.Error("CAUSE_SEARCH_ROUTE")

        val error = repository.setDestination(location).exceptionOrNull()

        assertTrue(error is NaviRequestException)
        assertEquals("CAUSE_SEARCH_ROUTE", (error as NaviRequestException).errorCode)
    }

    @Test
    fun noCompletionEvent_treatsRequestAsSent() = runTest {
        // 가이드: 이벤트는 내비 앱 구현에 따라 다를 수 있음 → 오류만 없으면 진행
        client.routeResponse = null

        assertTrue(repository.setDestination(location).isSuccess)
    }

    @Test
    fun waypoint_whileRouting_addsFirstSlot() = runTest {
        val result = repository.setWaypoint(location)

        assertEquals(NaviPlacement.Waypoint, result.getOrNull())
        assertEquals(listOf("waypoint:1"), client.calls)
    }

    @Test
    fun waypoint_withOneExisting_addsSecondSlot() = runTest {
        client.waypointCount = 1

        repository.setWaypoint(location)

        assertEquals(listOf("waypoint:2"), client.calls)
    }

    @Test
    fun waypoint_whenFull_failsWithoutRequest() = runTest {
        client.waypointCount = 2

        val error = repository.setWaypoint(location).exceptionOrNull()

        assertTrue(error is NaviWaypointFullException)
        assertTrue(client.calls.isEmpty())
    }

    @Test
    fun waypoint_withoutActiveRoute_setsDestinationInstead() = runTest {
        client.isGuiding = false

        val result = repository.setWaypoint(location)

        assertEquals(NaviPlacement.Destination, result.getOrNull())
        assertEquals(listOf("route"), client.calls)
    }

    @Test
    fun waypoint_routeStateNoResponse_failsWithoutTouchingNavi() = runTest {
        // 안내 중인지 모르는 채 목적지로 바꾸면 운전자의 기존 목적지가 사라질 수 있음 → 실패로 처리
        client.isGuiding = null

        val error = repository.setWaypoint(location).exceptionOrNull()

        assertTrue(error is NaviNoResponseException)
        assertTrue(client.calls.isEmpty())
    }

    @Test
    fun waypoint_naviError_fails() = runTest {
        client.waypointResponse = NaviEvent.Error("CAUSE_SEARCH_ROUTE")

        assertTrue(repository.setWaypoint(location).exceptionOrNull() is NaviRequestException)
    }

    @Test
    fun endGuidance_cancelsRoute_evenAfterWaypoint() = runTest {
        repository.setWaypoint(location)

        repository.endGuidance()

        // 경유지만 지우지 않고 안내 자체를 종료
        assertEquals(listOf("waypoint:1", "cancel"), client.calls)
    }

    @Test
    fun endGuidance_withoutConnection_fails() = runTest {
        client.connectResult = Result.failure(IllegalStateException("서비스 없음"))

        assertTrue(repository.endGuidance().exceptionOrNull() is NaviNotConnectedException)
        assertTrue(client.calls.isEmpty())
    }

    @Test
    fun arrivals_fromNaviArrivedEvent() = runTest {
        val arrived = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { repository.arrivals.collect { arrived += it } }

        client.send(NaviEvent.RouteStarted)
        assertTrue(arrived.isEmpty())
        client.send(NaviEvent.Arrived)

        assertEquals(1, arrived.size)
    }

    @Test
    fun arrivals_simulatedAfterDemoDelay() = runTest {
        val demo = PleosNaviRepository(client, 10.seconds, 5.seconds, 10.seconds, simulatedArrival = 20.seconds)
        val arrived = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { demo.arrivals.collect { arrived += it } }

        advanceTimeBy(19.seconds)
        runCurrent()
        assertTrue(arrived.isEmpty())

        advanceTimeBy(1.seconds)
        runCurrent()
        assertEquals(1, arrived.size)
    }

    @Test
    fun connect_isReused_acrossRequests() = runTest {
        repository.connect()
        repository.setDestination(location)
        repository.setWaypoint(location)

        assertEquals(1, client.connectCount)
    }

    @Test
    fun connectFailure_failsRequest_thenRetriesNextTime() = runTest {
        client.connectResult = Result.failure(IllegalStateException("서비스 없음"))
        assertTrue(repository.setDestination(location).exceptionOrNull() is NaviNotConnectedException)
        assertTrue(client.calls.isEmpty())

        client.connectResult = Result.success(Unit)
        assertTrue(repository.setDestination(location).isSuccess)
        assertEquals(2, client.connectCount)
    }

    @Test
    fun connectNoResponse_timesOut() = runTest {
        client.connectResult = null

        assertTrue(repository.setDestination(location).exceptionOrNull() is NaviNotConnectedException)
    }
}
