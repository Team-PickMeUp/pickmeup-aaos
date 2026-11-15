package com.example.pickmeup.data.navi

import com.example.pickmeup.data.runSuspendCatching
import com.example.pickmeup.model.LocationShareResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * NaviHelper SDK 로 Pleos 내비에 픽업 위치를 넘기는 저장소.
 *
 * - 요청은 결과를 바로 돌려주지 않아서, 요청 후 관련 이벤트를 기다립니다.
 * - 완료 이벤트가 정해진 시간 안에 안 오면 요청은 전달된 것으로 보고 진행합니다
 *   (가이드: 이벤트는 내비 앱 구현·상태에 따라 달라질 수 있음). 오류 이벤트가 오면 실패입니다.
 * - 상태 조회(안내 중인지, 경유지 개수)에 응답이 없으면 잘못된 판단을 막기 위해 실패로 처리합니다.
 */
@Singleton
class PleosNaviRepository internal constructor(
    private val client: NaviClient,
    private val connectTimeout: Duration,
    private val queryTimeout: Duration,
    private val completionTimeout: Duration,
    /** 시연용 도착 처리 시간. null 이면 내비 도착 이벤트만 사용 */
    private val simulatedArrival: Duration?,
) : NaviRepository {

    @Inject
    constructor(client: NaviClient) : this(
        client = client,
        connectTimeout = 10.seconds,
        queryTimeout = 5.seconds,
        // 실기기: requestRoute 후 내비의 「안내 시작」 카운트다운이 끝나야 onRouteStarted (약 7.5초)
        completionTimeout = 15.seconds,
        simulatedArrival = NaviRepository.DEMO_ARRIVAL_DELAY,
    )

    /** 연결 결과. 실패하면 다음 요청 때 다시 연결 */
    private var connection: CompletableDeferred<Unit>? = null

    /** 요청이 동시에 섞이지 않도록 (이벤트를 요청별로 구분할 수 없음) */
    private val requestLock = Mutex()

    override fun connect() {
        startConnection()
    }

    override val arrivals: Flow<Unit> = merge(
        client.events.filter { it is NaviEvent.Arrived }.map { },
        simulatedArrival?.let { delayTime -> flow { delay(delayTime); emit(Unit) } } ?: emptyFlow(),
    )

    override suspend fun setDestination(location: LocationShareResult.Allowed): Result<Unit> = runSuspendCatching {
        requestLock.withLock {
            ensureConnected()
            routeTo(location)
        }
    }

    override suspend fun setWaypoint(location: LocationShareResult.Allowed): Result<NaviPlacement> =
        runSuspendCatching {
            requestLock.withLock {
                ensureConnected()
                val isGuiding = query("경로 안내 상태", client::requestRouteState) { (it as? NaviEvent.RouteState)?.isGuiding }
                if (!isGuiding) {
                    // 목적지가 없으면 경유지를 넣을 수 없음 → 픽업 위치를 목적지로
                    routeTo(location)
                    return@withLock NaviPlacement.Destination
                }

                val count = query("경유지 목록", client::requestWaypoints) { (it as? NaviEvent.Waypoints)?.count }
                if (count >= MAX_WAYPOINTS) throw NaviWaypointFullException()
                val index = count + 1

                val event = awaitAfter(completionTimeout, { client.addWaypoint(location, index) }) {
                    it.takeIf { e -> e is NaviEvent.WaypointAdded || e.isRouteReady() || e is NaviEvent.Error }
                }
                if (event is NaviEvent.Error) throw NaviRequestException(event.code)
                NaviPlacement.Waypoint
            }
        }

    override suspend fun endGuidance(): Result<Unit> = runSuspendCatching {
        requestLock.withLock {
            ensureConnected()
            // 결과 이벤트(onRouteCancelled)는 기다리지 않음 — 안내가 없을 때도 호출될 수 있음
            client.cancelRoute()
        }
    }

    /** 목적지 설정 후 탐색 완료/오류 대기 */
    private suspend fun routeTo(location: LocationShareResult.Allowed) {
        val event = awaitAfter(completionTimeout, { client.requestRoute(location) }) {
            // onRouteCancelled 는 기존 경로가 취소될 때도 오므로 실패로 보지 않음 (실기기 확인)
            it.takeIf { e -> e.isRouteReady() || e is NaviEvent.Error }
        }
        if (event is NaviEvent.Error) throw NaviRequestException(event.code)
    }

    /** 상태 조회: 응답이 없으면 실패 */
    private suspend fun <T : Any> query(what: String, request: () -> Unit, pick: (NaviEvent) -> T?): T =
        awaitAfter(queryTimeout, request, pick) ?: throw NaviNoResponseException(what)

    /**
     * 이벤트 구독을 먼저 시작한 뒤 [request] 를 보내고, [pick] 이 값을 돌려주는 첫 이벤트를 기다립니다.
     * (구독 전에 이벤트가 오면 놓치므로 UNDISPATCHED 로 즉시 구독)
     */
    private suspend fun <T : Any> awaitAfter(timeout: Duration, request: () -> Unit, pick: (NaviEvent) -> T?): T? =
        coroutineScope {
            val waiter = async(start = CoroutineStart.UNDISPATCHED) {
                withTimeoutOrNull(timeout) { client.events.mapNotNull(pick).first() }
            }
            request()
            waiter.await()
        }

    /** 진행 중이거나 성공한 연결은 재사용, 실패한 연결은 새로 시작 */
    @Synchronized
    private fun startConnection(): CompletableDeferred<Unit> {
        // 예외로 완료된 Deferred 는 isCancelled == true
        connection?.takeUnless { it.isCancelled }?.let { return it }
        val deferred = CompletableDeferred<Unit>()
        connection = deferred
        client.connect(
            onConnected = { deferred.complete(Unit) },
            onFailed = { deferred.completeExceptionally(NaviNotConnectedException(it)) },
        )
        return deferred
    }

    private suspend fun ensureConnected() {
        val deferred = startConnection()
        val connected = withTimeoutOrNull(connectTimeout) { runSuspendCatching { deferred.await() } }
            ?: run {
                // 응답 없는 연결은 버리고 다음 요청 때 다시 시도
                synchronized(this) { if (connection === deferred) connection = null }
                throw NaviNotConnectedException()
            }
        connected.getOrThrow()
    }

    /** 경로가 잡혔다는 신호: 가이드의 onRoutingCompleted, 실제 내비의 onRouteStarted */
    private fun NaviEvent.isRouteReady() = this is NaviEvent.RouteStarted || this is NaviEvent.RoutingCompleted

    private companion object {
        /** Pleos 가이드: 경유지는 최대 두 개 (WaypointIndex.FIRST / SECOND) */
        const val MAX_WAYPOINTS = 2
    }
}
