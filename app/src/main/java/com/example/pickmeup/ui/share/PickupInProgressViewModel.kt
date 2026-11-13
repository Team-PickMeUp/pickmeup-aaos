package com.example.pickmeup.ui.share

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickmeup.data.message.PickupMessageRepository
import com.example.pickmeup.data.pickup.PickupRepository
import com.example.pickmeup.model.LocationShareResult
import com.example.pickmeup.model.PickupMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 픽업 종료 결과 → 결과 팝업 (Figma 8-1 취소 / 8-2 완료) */
enum class PickupResult { Cancelled, Completed }

/** 픽업 진행 중 화면 */
@HiltViewModel
class PickupInProgressViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val pickupRepository: PickupRepository,
    messageRepository: PickupMessageRepository,
) : ViewModel() {

    /**
     * 화면에 표시할 위치. 진입 시점 값을 고정해서 씁니다.
     * (완료/취소하면 저장소 값이 비워지는데, 그때 화면이 '데이터 없음'으로 바뀌지 않도록)
     * null 이면 진행 중인 픽업 없이 들어온 것 → 이전 화면으로
     */
    val location: LocationShareResult.Allowed? =
        pickupRepository.currentShare.value as? LocationShareResult.Allowed

    private val _isProcessing = MutableStateFlow(false)
    /** 완료/취소 처리 중 (버튼 중복 클릭 방지) */
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    /**
     * 완료/취소 처리 결과. null 이 아니면 결과 팝업 표시 → 확인 시 메인으로.
     * 화면 회전 후에도 팝업이 유지되도록 SavedStateHandle 에 저장
     */
    val result: StateFlow<PickupResult?> = savedStateHandle.getStateFlow(KEY_RESULT, null)

    /**
     * 대상자가 보낸 가장 최근 상황 메시지 → 알림 카드. 없으면 null.
     * TODO: TTS 발화, 발화 종료 5초 후 숨김, 같은 메시지 중복 억제 (Figma 9-6)
     */
    val message: StateFlow<PickupMessage?> = messageRepository.messages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // TODO: 내비게이션 연동 시 경로에서 대상자 위치 제거
    fun cancelPickup() = finish(PickupResult.Cancelled) { pickupRepository.cancelPickup() }

    fun completePickup() = finish(PickupResult.Completed) { pickupRepository.completePickup() }

    private fun finish(result: PickupResult, action: suspend () -> Result<Unit>) {
        if (_isProcessing.value || this.result.value != null) return
        _isProcessing.value = true
        viewModelScope.launch {
            action()
                .onSuccess { savedStateHandle[KEY_RESULT] = result }
                .onFailure { /* TODO: 실패 안내 UI (Figma 미정) */ }
            _isProcessing.value = false
        }
    }

    private companion object {
        const val KEY_RESULT = "pickupResult"
    }
}
