package com.example.pickmeup.data.pickup

import com.example.pickmeup.model.LocationShareResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 서버 없이 화면을 확인하기 위한 가짜 저장소.
 * 위치 요청은 항상 즉시 '허용'되고, 주소는 예시 값입니다.
 *
 * `local.properties` 에 `pickmeup.api.useFake=false` 를 넣으면 [RemotePickupRepository] 를 사용합니다.
 */
@Singleton
class FakePickupRepository @Inject constructor() : PickupRepository {

    private val _currentShare = MutableStateFlow<LocationShareResult?>(null)
    override val currentShare: StateFlow<LocationShareResult?> = _currentShare.asStateFlow()

    override suspend fun requestLocationShare(
        targetName: String,
        phoneNumber: String,
    ): Result<LocationShareResult> {
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

    override suspend fun cancelPickup(): Result<Unit> {
        _currentShare.value = null
        return Result.success(Unit)
    }
}
