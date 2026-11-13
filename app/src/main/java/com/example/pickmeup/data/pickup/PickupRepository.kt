package com.example.pickmeup.data.pickup

import com.example.pickmeup.model.LocationShareResult
import kotlinx.coroutines.flow.StateFlow

/**
 * 픽업 진행 상태 저장소.
 *
 * 위치 요청 → 공유 결과 → 픽업 진행 중 화면이 같은 픽업 정보를 보기 때문에
 * 화면끼리 값을 넘기지 않고 이 저장소의 [currentShare] 를 함께 구독합니다.
 */
interface PickupRepository {

    /** 진행 중인 픽업의 위치 공유 결과. 진행 중인 픽업이 없으면 null */
    val currentShare: StateFlow<LocationShareResult?>

    /** 대상자에게 위치 공유를 요청하고, 대상자의 응답(허용/미허용)을 돌려줍니다. */
    suspend fun requestLocationShare(targetName: String, phoneNumber: String): Result<LocationShareResult>

    /** 픽업 완료: 서버에 알리고 [currentShare] 를 비웁니다. */
    suspend fun completePickup(): Result<Unit>

    /** 픽업 취소: 서버에 알리고 [currentShare] 를 비웁니다. */
    suspend fun cancelPickup(): Result<Unit>
}
