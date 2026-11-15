package com.example.pickmeup.data.pickup

import com.example.pickmeup.model.LocationShareResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 서버 없이 화면을 확인하기 위한 가짜 저장소.
 * 위치 요청은 [responseDelay] 동안 기다린 뒤 '허용'되고([denyRequests] 면 '미허용'), 위치는 예시 값(실제 좌표)입니다.
 *
 * `local.properties` 에 `pickmeup.api.useFake=false` 를 넣으면 [RemotePickupRepository] 를 사용합니다.
 */
@Singleton
class FakePickupRepository internal constructor(
    /** 대상자 응답까지 걸리는 시간 (대기 팝업 확인용). 테스트에서는 0 */
    private val responseDelay: Duration,
    /** true 면 대상자가 위치 공유를 거부한 것으로 응답 (거부 팝업 확인·테스트용) */
    private val denyRequests: Boolean = false,
) : PickupRepository {

    @Inject
    constructor() : this(responseDelay = 3.seconds)

    private val _currentShare = MutableStateFlow<LocationShareResult.Allowed?>(null)
    override val currentShare: StateFlow<LocationShareResult.Allowed?> = _currentShare.asStateFlow()

    override suspend fun requestLocationShare(
        targetName: String,
        phoneNumber: String,
    ): Result<LocationShareResult> {
        delay(responseDelay)
        if (denyRequests) return Result.success(LocationShareResult.Denied(targetName))
        val result = LocationShareResult.Allowed(
            targetName = targetName,
            // 실제 내비에 넘겨도 경로가 나오도록 실제 좌표 (Pleos NaviHelper 가이드 예제 위치)
            address = "서울시 강남구 남부순환로 2621",
            latitude = 37.48544722,
            longitude = 127.03636666,
        )
        _currentShare.value = result
        return Result.success(result)
    }

    override suspend fun completePickup(): Result<Unit> {
        _currentShare.value = null
        return Result.success(Unit)
    }

    override suspend fun cancelLocationRequest(): Result<Unit> {
        _currentShare.value = null
        return Result.success(Unit)
    }

    override suspend fun cancelPickup(): Result<Unit> {
        _currentShare.value = null
        return Result.success(Unit)
    }
}
