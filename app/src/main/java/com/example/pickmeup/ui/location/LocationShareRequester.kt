package com.example.pickmeup.ui.location

import com.example.pickmeup.data.pickup.PickupRepository
import com.example.pickmeup.data.pickup.ResponseTimeoutException
import com.example.pickmeup.model.LocationShareResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** 위치 공유 요청의 진행 상태 → 화면에 띄울 팝업 */
sealed interface LocationShareRequestState {
    /** 요청 없음 (팝업 없음) */
    data object Idle : LocationShareRequestState

    /** 대상자 응답 대기 중 (Figma 4 대기 팝업). [remainingSeconds] 는 자동 취소까지 남은 시간 */
    data class Waiting(val remainingSeconds: Int) : LocationShareRequestState

    /** 5분 안에 응답이 없어 자동 취소됨 (Figma 4-2) */
    data object TimedOut : LocationShareRequestState

    /** 대상자가 위치 공유를 거부함 (Figma 4-3) */
    data object Denied : LocationShareRequestState
}

sealed interface LocationShareEvent {
    /** 대상자가 허용함 → 경유지 / 목적지 선택 화면으로 */
    data object Allowed : LocationShareEvent

    // TODO: 실패 안내 UI (Figma 미정, Pleos 에서는 Toast 가 보이지 않음)
    data class RequestFailed(val cause: Throwable) : LocationShareEvent
}

/**
 * 위치 공유 요청 → 응답 대기(5분 카운트다운) → 허용 / 거부 / 시간 초과 처리.
 *
 * 위치 요청 화면(직접 입력)과 픽업 불러오기 화면(저장된 프로필 선택, Figma 2-1-4-1)이
 * 같은 흐름을 쓰기 때문에 ViewModel 에서 분리했습니다. 각 ViewModel 이 viewModelScope 로 만들어 씁니다.
 */
class LocationShareRequester(
    private val scope: CoroutineScope,
    private val repository: PickupRepository,
    private val timeout: Duration = RESPONSE_TIMEOUT,
) {

    private val _state = MutableStateFlow<LocationShareRequestState>(LocationShareRequestState.Idle)
    val state: StateFlow<LocationShareRequestState> = _state.asStateFlow()

    private val _events = Channel<LocationShareEvent>(Channel.BUFFERED)
    val events: Flow<LocationShareEvent> = _events.receiveAsFlow()

    /**
     * 응답을 기다리는 중인 요청 (취소용).
     * 즉시 실행 디스패처(Main.immediate)에서는 할당 전에 끝날 수도 있어서 null 대신 isActive 로 판단합니다.
     */
    private var requestJob: Job? = null

    /**
     * 위치 공유 요청. 이미 요청 중이거나 결과 팝업이 떠 있으면 무시합니다.
     * @param onAllowed 허용 직후, 화면 이동 이벤트를 보내기 전에 할 일 (예: 대상자 저장)
     */
    fun request(targetName: String, phoneDigits: String, onAllowed: suspend () -> Unit = {}) {
        if (requestJob?.isActive == true || _state.value != LocationShareRequestState.Idle) return

        requestJob = scope.launch {
            try {
                val result = coroutineScope {
                    val countdown = launch { countDown() }
                    // 시간 초과면 null (진행 중인 응답 대기도 함께 취소됨)
                    withTimeoutOrNull(timeout) {
                        repository.requestLocationShare(targetName, phoneDigits)
                    }.also { countdown.cancel() }
                }
                handle(result, onAllowed)
            } finally {
                // 요청 취소로 끝났으면 대기 팝업 닫기
                if (_state.value is LocationShareRequestState.Waiting) {
                    _state.value = LocationShareRequestState.Idle
                }
            }
        }
    }

    /**
     * 대기 팝업의 "요청 취소" (또는 뒤로 가기).
     * 응답 기다리기를 멈추고 서버에 취소를 알림 (대상자 웹 링크 즉시 무효화, Figma 9-4)
     */
    fun cancel() {
        val job = requestJob?.takeIf { it.isActive } ?: return
        job.cancel()
        _state.value = LocationShareRequestState.Idle
        scope.launch {
            // TODO: 취소 실패 안내 UI (Figma 미정)
            repository.cancelLocationRequest()
        }
    }

    /** 거부 / 자동 취소 팝업의 "확인" → 팝업 닫고 원래 화면에 머묾 */
    fun dismissResult() {
        val current = _state.value
        if (current == LocationShareRequestState.TimedOut || current == LocationShareRequestState.Denied) {
            _state.value = LocationShareRequestState.Idle
        }
    }

    private suspend fun countDown() {
        var remaining = timeout.inWholeSeconds.toInt()
        while (true) {
            _state.value = LocationShareRequestState.Waiting(remaining)
            if (remaining == 0) return
            delay(1.seconds)
            remaining--
        }
    }

    private suspend fun handle(result: Result<LocationShareResult>?, onAllowed: suspend () -> Unit) {
        val share = result?.getOrNull()
        val error = result?.exceptionOrNull()
        when {
            result == null || error is ResponseTimeoutException -> timeOut()
            error != null -> {
                _state.value = LocationShareRequestState.Idle
                _events.send(LocationShareEvent.RequestFailed(error))
            }
            share is LocationShareResult.Denied -> _state.value = LocationShareRequestState.Denied
            share is LocationShareResult.Allowed -> {
                onAllowed()
                _state.value = LocationShareRequestState.Idle
                _events.send(LocationShareEvent.Allowed)
            }
        }
    }

    /** 5분 무응답: 서버에 요청 취소를 알려 대상자 링크를 막고 자동 취소 팝업 표시 */
    private suspend fun timeOut() {
        _state.value = LocationShareRequestState.TimedOut
        repository.cancelLocationRequest()
    }

    companion object {
        /** Figma 4: "5분 안에 위치가 공유되지 않으면 요청이 자동으로 취소돼요" */
        val RESPONSE_TIMEOUT = 5.minutes
    }
}
