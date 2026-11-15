package com.example.pickmeup.data.navi

import com.example.pickmeup.model.LocationShareResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pleos 내비 서비스가 없는 환경(일반 에뮬레이터, 테스트)용 가짜 내비.
 * 요청은 항상 성공하고, 무엇을 요청했는지만 기록합니다.
 *
 * `local.properties` 에 `pickmeup.navi.useFake=true` 를 넣으면 사용합니다.
 */
@Singleton
class FakeNaviRepository internal constructor(
    /** 도착 처리 시간. null 이면 도착하지 않음 (테스트용) */
    private val simulatedArrival: Duration?,
) : NaviRepository {

    @Inject
    constructor() : this(simulatedArrival = NaviRepository.DEMO_ARRIVAL_DELAY)

    /** 마지막으로 넘긴 위치와 방식. 안내를 종료하면 null */
    var placed: Pair<LocationShareResult.Allowed, NaviPlacement>? = null
        private set

    /** 안내 종료를 요청한 횟수 */
    var endGuidanceCount = 0
        private set

    override fun connect() = Unit

    override val arrivals: Flow<Unit> =
        simulatedArrival?.let { delayTime -> flow { delay(delayTime); emit(Unit) } } ?: emptyFlow()

    override suspend fun setDestination(location: LocationShareResult.Allowed): Result<Unit> {
        placed = location to NaviPlacement.Destination
        return Result.success(Unit)
    }

    override suspend fun setWaypoint(location: LocationShareResult.Allowed): Result<NaviPlacement> {
        placed = location to NaviPlacement.Waypoint
        return Result.success(NaviPlacement.Waypoint)
    }

    override suspend fun endGuidance(): Result<Unit> {
        endGuidanceCount++
        placed = null
        return Result.success(Unit)
    }
}
