package com.example.pickmeup.data.pickup

import com.example.pickmeup.data.remote.PickupApi
import com.example.pickmeup.data.remote.dto.CreateLocationRequestBody
import com.example.pickmeup.data.remote.dto.LocationRequestResponse
import com.example.pickmeup.data.remote.dto.LocationRequestStatus
import com.example.pickmeup.model.LocationShareResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * 서버([PickupApi])와 통신하는 저장소.
 *
 * ⚠️ 초안: 대상자 응답은 일정 간격으로 상태를 조회(polling)해서 기다립니다.
 * 서버가 푸시(FCM)나 WebSocket 으로 알려주는 방식이 되면 이 부분을 바꾸세요.
 */
@Singleton
class RemotePickupRepository @Inject constructor(
    private val api: PickupApi,
) : PickupRepository {

    private val _currentShare = MutableStateFlow<LocationShareResult?>(null)
    override val currentShare: StateFlow<LocationShareResult?> = _currentShare.asStateFlow()

    /** 완료/취소 API 에 필요한 현재 요청 ID */
    private var currentRequestId: Long? = null

    override suspend fun requestLocationShare(
        targetName: String,
        phoneNumber: String,
    ): Result<LocationShareResult> = runSuspendCatching {
        val created = api.createLocationRequest(CreateLocationRequestBody(targetName, phoneNumber))
        currentRequestId = created.id

        val answered = withTimeout(RESPONSE_TIMEOUT) {
            var response = created
            while (response.status == LocationRequestStatus.PENDING) {
                delay(POLL_INTERVAL)
                response = api.getLocationRequest(created.id)
            }
            response
        }
        answered.toShareResult().also { _currentShare.value = it }
    }

    override suspend fun completePickup(): Result<Unit> = runSuspendCatching {
        currentRequestId?.let { api.completePickup(it) }
        clear()
    }

    override suspend fun cancelPickup(): Result<Unit> = runSuspendCatching {
        currentRequestId?.let { api.cancelPickup(it) }
        clear()
    }

    private fun clear() {
        currentRequestId = null
        _currentShare.value = null
    }

    private fun LocationRequestResponse.toShareResult(): LocationShareResult = when (status) {
        LocationRequestStatus.ALLOWED -> LocationShareResult.Allowed(
            targetName = targetName,
            address = requireNotNull(address) { "허용 응답에 주소가 없습니다" },
            latitude = requireNotNull(latitude) { "허용 응답에 위도가 없습니다" },
            longitude = requireNotNull(longitude) { "허용 응답에 경도가 없습니다" },
        )
        else -> LocationShareResult.Denied(targetName)
    }

    private companion object {
        val POLL_INTERVAL = 3.seconds
        val RESPONSE_TIMEOUT = 5.minutes
    }
}

/** runCatching 과 같지만 코루틴 취소(CancellationException)는 삼키지 않습니다. */
internal inline fun <T> runSuspendCatching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
