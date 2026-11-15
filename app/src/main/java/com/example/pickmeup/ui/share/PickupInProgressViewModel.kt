package com.example.pickmeup.ui.share

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickmeup.data.message.PickupMessageRepository
import com.example.pickmeup.data.navi.NaviRepository
import com.example.pickmeup.data.pickup.PickupRepository
import com.example.pickmeup.data.tts.SpeechRepository
import com.example.pickmeup.model.LocationShareResult
import com.example.pickmeup.model.PickupMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/** 픽업 종료 결과 → 결과 팝업 (Figma 8-1 취소 / 8-2 완료) */
enum class PickupResult { Cancelled, Completed }

/** 픽업 진행 중 화면 */
@HiltViewModel
class PickupInProgressViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val pickupRepository: PickupRepository,
    messageRepository: PickupMessageRepository,
    private val naviRepository: NaviRepository,
    private val speechRepository: SpeechRepository,
) : ViewModel() {

    /**
     * 화면에 표시할 위치. 진입 시점 값을 고정해서 씁니다.
     * (완료/취소하면 저장소 값이 비워지는데, 그때 화면이 '데이터 없음'으로 바뀌지 않도록)
     * null 이면 진행 중인 픽업 없이 들어온 것 → 이전 화면으로
     */
    val location: LocationShareResult.Allowed? = pickupRepository.currentShare.value

    private val _isProcessing = MutableStateFlow(false)
    /** 완료/취소 처리 중 (버튼 중복 클릭 방지) */
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    /**
     * 완료/취소 처리 결과. null 이 아니면 결과 팝업 표시 → 확인 시 첫 화면(픽업 불러오기)으로.
     * 화면 회전 후에도 팝업이 유지되도록 SavedStateHandle 에 저장
     */
    val result: StateFlow<PickupResult?> = savedStateHandle.getStateFlow(KEY_RESULT, null)

    private val _message = MutableStateFlow<PickupMessage?>(null)
    /**
     * 지금 보여줄 대상자 상황 알림. 알림이 뜰 때 같은 문구를 TTS 로 읽어주고 (Figma 9-6),
     * **발화가 끝나면** 다시 null (최소 [MIN_NOTIFICATION_DURATION], TTS 를 못 쓰면 이 시간만 노출).
     * 알림 사이에는 최소 [NOTIFICATION_GAP] 간격을 둡니다 (그 사이 도착한 메시지는 순서대로 대기).
     */
    val message: StateFlow<PickupMessage?> = _message.asStateFlow()

    private val _arrived = MutableStateFlow(false)
    /** 픽업 위치에 도착 → 카드 문구가 도착 안내로 바뀜 */
    val arrived: StateFlow<Boolean> = _arrived.asStateFlow()

    init {
        // 화면 진입(= 내비 안내 시작) 시점부터 메시지·도착 시나리오 시작
        viewModelScope.launch {
            // buffer: 알림을 보여주거나 간격을 두는 동안 도착한 메시지는 버리지 않고 순서대로 보여줌
            messageRepository.messages.buffer(Channel.UNLIMITED).collect { message ->
                if (result.value != null) return@collect
                _message.value = message
                coroutineScope {
                    val minimum = launch { delay(MIN_NOTIFICATION_DURATION) }
                    speechRepository.speak(message.textRes) // 발화가 끝날 때까지 기다림
                    minimum.join()
                }
                _message.value = null
                delay(NOTIFICATION_GAP) // 다음 알림까지 쉬는 시간
            }
        }
        viewModelScope.launch {
            naviRepository.arrivals.first()
            _arrived.value = true
        }
    }

    fun cancelPickup() = finish(PickupResult.Cancelled) { pickupRepository.cancelPickup() }

    fun completePickup() = finish(PickupResult.Completed) { pickupRepository.completePickup() }

    private fun finish(result: PickupResult, action: suspend () -> Result<Unit>) {
        if (_isProcessing.value || this.result.value != null) return
        _isProcessing.value = true
        viewModelScope.launch {
            action()
                .onSuccess {
                    // 픽업이 끝났으니 내비 안내 종료. 실패해도 픽업 종료는 진행 (운전자가 내비에서 직접 종료 가능)
                    naviRepository.endGuidance()
                    _message.value = null
                    speechRepository.stop()
                    savedStateHandle[KEY_RESULT] = result
                }
                .onFailure { /* TODO: 실패 안내 UI (Figma 미정) */ }
            _isProcessing.value = false
        }
    }

    override fun onCleared() {
        // 화면을 벗어나면 읽던 안내 중단
        speechRepository.stop()
    }

    companion object {
        /** 알림 최소 노출 시간 (발화가 더 짧거나 TTS 를 쓸 수 없을 때) */
        val MIN_NOTIFICATION_DURATION = 2.seconds

        /** 알림이 올라간 뒤 다음 알림이 내려오기까지 최소 간격 */
        val NOTIFICATION_GAP = 2.seconds

        private const val KEY_RESULT = "pickupResult"
    }
}
