package com.example.pickmeup.ui.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickmeup.data.pickup.PickupRepository
import com.example.pickmeup.model.LocationShareResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 픽업 진행 중 화면 */
@HiltViewModel
class PickupInProgressViewModel @Inject constructor(
    private val pickupRepository: PickupRepository,
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

    private val _finished = Channel<Unit>(Channel.BUFFERED)
    /** 픽업 종료(완료/취소) → 메인으로 */
    val finished: Flow<Unit> = _finished.receiveAsFlow()

    // TODO: 취소 확인 팝업 여부 (기획 결정 필요), 내비 경로에서 제거
    fun cancelPickup() = finish { pickupRepository.cancelPickup() }

    fun completePickup() = finish { pickupRepository.completePickup() }

    private fun finish(action: suspend () -> Result<Unit>) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            action()
                .onSuccess { _finished.send(Unit) }
                .onFailure { /* TODO: 실패 안내 UI (Figma 미정) */ }
            _isProcessing.value = false
        }
    }
}
