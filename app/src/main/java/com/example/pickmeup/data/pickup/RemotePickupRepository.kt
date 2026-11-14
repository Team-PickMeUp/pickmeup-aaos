package com.example.pickmeup.data.pickup

import com.example.pickmeup.data.remote.PickupApi
import com.example.pickmeup.data.remote.dto.CreateLocationRequestBody
import com.example.pickmeup.data.remote.dto.LocationRequestResponse
import com.example.pickmeup.data.remote.dto.LocationRequestStatus
import com.example.pickmeup.data.runSuspendCatching
import com.example.pickmeup.model.LocationShareResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull
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

    private val _currentShare = MutableStateFlow<LocationShareResult.Allowed?>(null)
    override val currentShare: StateFlow<LocationShareResult.Allowed?> = _currentShare.asStateFlow()

    /** 완료/취소 API 에 필요한 현재 요청 ID */
    private var currentRequestId: Long? = null

    override suspend fun requestLocationShare(
        targetName: String,
        phoneNumber: String,
    ): Result<LocationShareResult> = runSuspendCatching {
        val created = api.createLocationRequest(CreateLocationRequestBody(targetName, phoneNumber))
        currentRequestId = created.id

        // 화면 쪽 5분 카운트다운이 먼저 요청을 취소하므로, 이 제한은 서버가 응답을 안 줄 때를 대비한 안전장치입니다.
        // withTimeout 은 시간 초과를 '취소'로 던져서 runSuspendCatching 이 실패로 잡지 못함 → OrNull 사용
        val answered = withTimeoutOrNull(RESPONSE_TIMEOUT) {
            var response = created
            while (response.status == LocationRequestStatus.PENDING) {
                delay(POLL_INTERVAL)
                response = api.getLocationRequest(created.id)
            }
            response
        } ?: throw ResponseTimeoutException()
        answered.toShareResult().also { result ->
            when (result) {
                is LocationShareResult.Allowed -> _currentShare.value = result
                // 미허용: 픽업이 시작되지 않았으므로 진행 중인 요청도 비움
                is LocationShareResult.Denied -> currentRequestId = null
            }
        }
    }

    override suspend fun cancelLocationRequest(): Result<Unit> = runSuspendCatching {
        currentRequestId?.let { api.cancelLocationRequest(it) }
        clear()
    }

    override suspend fun completePickup(): Result<Unit> = runSuspendCatching {
        currentRequestId?.let { api.completePickup(it) }
        clear()
    }

    override suspend fun cancelPickup(): Result<Unit> = runSuspendCatching {
        currentRequestId?.let { api.cancelLocationRequest(it) }
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
        /** 화면 카운트다운(5분)보다 조금 길게 → 정상적으로는 화면 쪽 시간 초과가 먼저 처리됨 */
        val RESPONSE_TIMEOUT = 5.minutes + 10.seconds
    }
}

/** 대상자가 정해진 시간 안에 응답하지 않음 */
class ResponseTimeoutException : Exception("픽업 대상자 응답 시간 초과")
