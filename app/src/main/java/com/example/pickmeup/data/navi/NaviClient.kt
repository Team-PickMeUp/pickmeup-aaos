package com.example.pickmeup.data.navi

import com.example.pickmeup.model.LocationShareResult
import kotlinx.coroutines.flow.Flow

/**
 * NaviHelper SDK 를 감싼 얇은 인터페이스.
 *
 * SDK 클래스(NaviHelper, NaviHelperEventListener, SDK 데이터 모델)는 [NaviHelperClient] 에만 두고,
 * 경유지 인덱스 선택·이벤트 대기 같은 판단은 [PleosNaviRepository] 에서 해서 단위 테스트할 수 있게 합니다.
 *
 * SDK 규칙 (Pleos 가이드):
 * - 요청 API 는 결과를 바로 돌려주지 않고 [events] 로 알려줍니다.
 * - 이벤트 순서는 내비 앱 구현·상태에 따라 달라질 수 있어 고정 순서를 가정하지 않습니다.
 */
interface NaviClient {

    /** 내비 이벤트 (SDK 리스너 콜백을 변환) */
    val events: Flow<NaviEvent>

    /** `initialize(onConnected, onFailed)` → 연결되면 리스너 등록 */
    fun connect(onConnected: () -> Unit, onFailed: (Throwable) -> Unit)

    /** `requestRoute(RouteInfo)` — 목적지 설정 */
    fun requestRoute(location: LocationShareResult.Allowed)

    /** `addWaypoint(RequestWaypointInfo)` — [index] 는 1(FIRST) 또는 2(SECOND) */
    fun addWaypoint(location: LocationShareResult.Allowed, index: Int)

    /** `cancelRoute()` — 목적지 취소 */
    fun cancelRoute()

    /** `getRouteStateInfo()` → [NaviEvent.RouteState] */
    fun requestRouteState()

    /** `getWaypointInfo()` → [NaviEvent.Waypoints] */
    fun requestWaypoints()
}

/** 앱에서 쓰는 내비 이벤트만 추린 것 */
sealed interface NaviEvent {
    /**
     * onRouteStateInfo — 목적지 안내 중인지 (naviMode == ROUTE_GUIDING / SIMULATION_GUIDING).
     * SDK 의 `isRouting` 은 '경로 계산 중'이라 안내 여부 판단에 쓰면 안 됨 (실기기 확인)
     */
    data class RouteState(val isGuiding: Boolean) : NaviEvent

    /** onWayPointInfo — 현재 경유지 개수 */
    data class Waypoints(val count: Int) : NaviEvent

    /** onRouteStarted — 경로 안내 시작 (실제 내비는 탐색 완료 대신 이 이벤트를 보냄) */
    data object RouteStarted : NaviEvent

    /** onRoutingCompleted — 경로 탐색 완료 (가이드에는 있으나 Pleos 내비 3.0.4 에서는 오지 않음) */
    data object RoutingCompleted : NaviEvent

    /** onWaypointChanged(ADD) — 경유지 추가됨 */
    data object WaypointAdded : NaviEvent

    /** onDestinationArrived / onWayPointArrived — 목적지·경유지 도착 */
    data object Arrived : NaviEvent

    /** onError — 요청 처리 실패 */
    data class Error(val code: String) : NaviEvent
}
