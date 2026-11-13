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
 * 위치 요청은 [responseDelay] 동안 기다린 뒤 항상 '허용'되고, 주소는 예시 값입니다.
 *
 * `local.properties` 에 `pickmeup.api.useFake=false` 를 넣으면 [RemotePickupRepository] 를 사용합니다.
 */
@Singleton
class FakePickupRepository internal constructor(
    /** 대상자 응답까지 걸리는 시간 (대기 팝업 확인용). 테스트에서는 0 */
    private val responseDelay: Duration,
) : PickupRepository {

    @Inject
    constructor() : this(responseDelay = 3.seconds)

    private val _currentShare = MutableStateFlow<LocationShareResult?>(null)
    override val currentShare: StateFlow<LocationShareResult?> = _currentShare.asStateFlow()

    override suspend fun requestLocationShare(
        targetName: String,
        phoneNumber: String,
    ): Result<LocationShareResult> {
        delay(responseDelay)
        val result = LocationShareResult.Allowed(
            targetName = targetName,
            address = "00시 00구 00로",
            latitude = 0.0,
            longitude = 0.0,
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
