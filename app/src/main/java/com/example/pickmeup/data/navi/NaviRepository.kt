package com.example.pickmeup.data.navi

import com.example.pickmeup.model.LocationShareResult
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.seconds

/**
 * 차량 내비게이션(Pleos Navi Service)에 픽업 위치를 넘기는 저장소.
 *
 * - 실제 구현: [PleosNaviRepository] (NaviHelper SDK)
 * - 내비 서비스가 없는 환경: [FakeNaviRepository] (local.properties `pickmeup.navi.useFake=true`)
 */
interface NaviRepository {

    /** 내비 서비스 연결 시작 (앱 시작 시 한 번). 결과를 기다리지 않습니다. */
    fun connect()

    /**
     * 픽업 위치(경유지 또는 목적지)에 도착하면 한 번 알림. 구독을 시작한 때부터 셉니다.
     * TODO: 시연용 — 차량이 실제로 움직이지 않아 [DEMO_ARRIVAL_DELAY] 뒤 도착한 것으로 처리. 실차에서는 내비 도착 이벤트만 사용
     */
    val arrivals: Flow<Unit>

    /**
     * 픽업 위치를 목적지로 설정합니다. 기존 목적지는 바뀝니다.
     * 내비가 경로 탐색을 끝내거나 오류를 알릴 때까지 기다립니다.
     */
    suspend fun setDestination(location: LocationShareResult.Allowed): Result<Unit>

    /**
     * 픽업 위치를 경유지로 추가합니다.
     * 내비 안내 중이 아니면(목적지 없음) 경유지를 넣을 수 없어서 목적지로 설정합니다.
     *
     * @return 실제로 설정된 방식 ([NaviPlacement.Destination] 이면 목적지로 대신 설정됨)
     */
    suspend fun setWaypoint(location: LocationShareResult.Allowed): Result<NaviPlacement>

    /** 픽업이 끝나면(완료·취소) 내비 안내를 종료합니다 (`cancelRoute`). */
    suspend fun endGuidance(): Result<Unit>

    companion object {
        /** 시연용: 안내 시작 후 픽업 위치 도착으로 처리하기까지 걸리는 시간 */
        val DEMO_ARRIVAL_DELAY = 20.seconds
    }
}

/** 픽업 위치가 내비에 들어간 방식 */
enum class NaviPlacement { Waypoint, Destination }

/** 내비 서비스에 연결하지 못함 (서비스 미설치, 연결 재시도 실패, 시간 초과) */
class NaviNotConnectedException(cause: Throwable? = null) : Exception("내비 서비스에 연결하지 못했습니다", cause)

/** 내비가 요청 처리 중 오류를 알림 (NaviErrorCode 이름) */
class NaviRequestException(val errorCode: String) : Exception("내비 요청 실패: $errorCode")

/** 경유지가 이미 최대 개수(2개)라 더 넣을 수 없음 */
class NaviWaypointFullException : Exception("경유지는 최대 2개까지 설정할 수 있습니다")

/** 내비 상태 조회에 응답이 없음 */
class NaviNoResponseException(what: String) : Exception("내비 응답 없음: $what")
