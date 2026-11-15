package com.example.pickmeup.data.navi

import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.constants.NaviMode
import ai.pleos.playground.navi.constants.WaypointCmdType
import ai.pleos.playground.navi.constants.WaypointIndex
import ai.pleos.playground.navi.data.DestinationArrivedInfo
import ai.pleos.playground.navi.data.RequestWaypointInfo
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.data.RouteStartInfo
import ai.pleos.playground.navi.data.RouteStateInfo
import ai.pleos.playground.navi.data.WaypointArrivedInfo
import ai.pleos.playground.navi.data.WaypointChangedInfo
import ai.pleos.playground.navi.data.WaypointInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import android.content.Context
import android.util.Log
import com.example.pickmeup.model.LocationShareResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NaviHelper SDK 실제 호출부. SDK 클래스는 이 파일에서만 씁니다.
 *
 * Pleos 가이드 순서: `NaviHelper(applicationContext)` → `initialize(onConnected, onFailed)`
 * → 연결되면 `addListener()` → API 호출 → 다 쓰면 `removeListener()` → `release()`.
 * 앱 전체에서 하나만 쓰는 싱글턴이라 앱이 살아 있는 동안 연결을 유지합니다.
 */
@Singleton
class NaviHelperClient @Inject constructor(
    @ApplicationContext private val context: Context,
) : NaviClient {

    private val helper by lazy { NaviHelper(context) }

    private val _events = MutableSharedFlow<NaviEvent>(extraBufferCapacity = 16)
    override val events: Flow<NaviEvent> = _events.asSharedFlow()

    /** SDK 콜백(바인더 스레드) → [events]. tryEmit 은 스레드 안전 */
    private val listener = object : NaviHelperEventListener {
        override fun onRouteStateInfo(routeStateInfo: RouteStateInfo) {
            val guiding = routeStateInfo.naviMode == NaviMode.ROUTE_GUIDING ||
                routeStateInfo.naviMode == NaviMode.SIMULATION_GUIDING
            Log.d(TAG, "routeState naviMode=${routeStateInfo.naviMode} isRouting=${routeStateInfo.isRouting}")
            emit(NaviEvent.RouteState(isGuiding = guiding))
        }

        override fun onWayPointInfo(waypointInfoList: List<WaypointInfo>) {
            emit(NaviEvent.Waypoints(waypointInfoList.size))
        }

        override fun onRouteStarted(routeStartInfo: RouteStartInfo) {
            emit(NaviEvent.RouteStarted)
        }

        override fun onRoutingCompleted() {
            emit(NaviEvent.RoutingCompleted)
        }

        override fun onWaypointChanged(waypointChangedInfo: WaypointChangedInfo) {
            Log.d(TAG, "waypointChanged ${waypointChangedInfo.cmdType} ${waypointChangedInfo.index}")
            if (waypointChangedInfo.cmdType == WaypointCmdType.ADD) emit(NaviEvent.WaypointAdded)
        }

        override fun onDestinationArrived(destinationArrivedInfo: DestinationArrivedInfo) {
            emit(NaviEvent.Arrived)
        }

        override fun onWayPointArrived(waypointArrivedInfo: WaypointArrivedInfo) {
            emit(NaviEvent.Arrived)
        }

        override fun onError(errorCode: NaviErrorCode) {
            emit(NaviEvent.Error(errorCode.name))
        }
    }

    private fun emit(event: NaviEvent) {
        Log.d(TAG, "event $event")
        _events.tryEmit(event)
    }

    override fun connect(onConnected: () -> Unit, onFailed: (Throwable) -> Unit) {
        helper.initialize(
            {
                // 가이드: 최초 연결 성공 후 리스너를 등록하고 API 를 호출
                helper.addListener(listener)
                onConnected()
            },
            { throwable -> onFailed(throwable) },
        )
    }

    override fun requestRoute(location: LocationShareResult.Allowed) {
        helper.requestRoute(
            RouteInfo(
                latitude = location.latitude,
                longitude = location.longitude,
                displayLatitude = null,
                displayLongitude = null,
                poiName = location.targetName,
                poiId = "",
                poiSubId = "",
                address = location.address,
            ),
        )
    }

    override fun addWaypoint(location: LocationShareResult.Allowed, index: Int) {
        helper.addWaypoint(
            RequestWaypointInfo(
                latitude = location.latitude,
                longitude = location.longitude,
                poiName = location.targetName,
                waypointIndex = index.toWaypointIndex(),
                poiId = "",
                poiSubId = "",
                address = location.address,
            ),
        )
    }

    override fun cancelRoute() = helper.cancelRoute()

    override fun requestRouteState() = helper.getRouteStateInfo()

    override fun requestWaypoints() = helper.getWaypointInfo()

    private companion object {
        const val TAG = "PickMeUpNavi"
    }

    private fun Int.toWaypointIndex(): WaypointIndex = when (this) {
        1 -> WaypointIndex.FIRST
        2 -> WaypointIndex.SECOND
        else -> throw IllegalArgumentException("경유지 인덱스는 1 또는 2: $this")
    }
}
