package com.example.pickmeup.ui.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickmeup.data.pickup.PickupRepository
import com.example.pickmeup.model.LocationShareResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 경유지 / 목적지 선택 화면 */
@HiltViewModel
class ShareResultViewModel @Inject constructor(
    pickupRepository: PickupRepository,
) : ViewModel() {

    /** null 이면 진행 중인 픽업이 없음 → 이전 화면으로 */
    val shareResult: StateFlow<LocationShareResult?> = pickupRepository.currentShare

    private val _pickupStarted = Channel<Unit>(Channel.BUFFERED)
    /** 경유지/목적지 설정 완료 → 픽업 진행 중 화면으로 */
    val pickupStarted: Flow<Unit> = _pickupStarted.receiveAsFlow()

    fun setAsWaypoint(location: LocationShareResult.Allowed) {
        // TODO: 내비게이션 연동 — location 을 경유지로 추가 (Pleos 내비 API 확인 필요)
        viewModelScope.launch { _pickupStarted.send(Unit) }
    }

    fun setAsDestination(location: LocationShareResult.Allowed) {
        // TODO: 내비게이션 연동 — location 을 목적지로 설정 (Pleos 내비 API 확인 필요)
        viewModelScope.launch { _pickupStarted.send(Unit) }
    }
}
