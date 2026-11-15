package com.example.pickmeup.ui.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickmeup.data.navi.NaviRepository
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

/** 경유지 / 목적지 선택 화면 */
@HiltViewModel
class ShareResultViewModel @Inject constructor(
    pickupRepository: PickupRepository,
    private val naviRepository: NaviRepository,
) : ViewModel() {

    /** null 이면 진행 중인 픽업이 없음 → 이전 화면으로 */
    val shareResult: StateFlow<LocationShareResult.Allowed?> = pickupRepository.currentShare

    private val _isProcessing = MutableStateFlow(false)
    /** 내비에 위치를 넘기는 중 (버튼 중복 클릭 방지) */
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _pickupStarted = Channel<Unit>(Channel.BUFFERED)
    /** 내비에 경유지/목적지 설정 완료 → 픽업 진행 중 화면으로 */
    val pickupStarted: Flow<Unit> = _pickupStarted.receiveAsFlow()

    /** 경유지로 추가. 내비 안내 중이 아니면 저장소가 목적지로 대신 설정 */
    fun setAsWaypoint(location: LocationShareResult.Allowed) = sendToNavi { naviRepository.setWaypoint(location) }

    fun setAsDestination(location: LocationShareResult.Allowed) = sendToNavi { naviRepository.setDestination(location) }

    private fun sendToNavi(request: suspend () -> Result<*>) {
        if (_isProcessing.value) return
        _isProcessing.value = true
        viewModelScope.launch {
            request()
                .onSuccess { _pickupStarted.send(Unit) }
                // TODO: 실패 안내 UI (Figma 미정). 버튼이 다시 활성화되어 재시도 가능
                .onFailure { }
            _isProcessing.value = false
        }
    }
}
