package com.example.pickmeup.ui.target

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickmeup.data.pickup.PickupRepository
import com.example.pickmeup.data.target.PickupTargetRepository
import com.example.pickmeup.model.PickupTarget
import com.example.pickmeup.ui.location.LocationShareEvent
import com.example.pickmeup.ui.location.LocationShareRequestState
import com.example.pickmeup.ui.location.LocationShareRequester
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 픽업 불러오기 화면 (앱 첫 화면).
 * 프로필 선택 → 확인 팝업(Figma 2-1-4) → 이 화면에서 바로 위치 요청 + 대기 팝업(Figma 2-1-4-1)
 */
@HiltViewModel
class PickupTargetSelectViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: PickupTargetRepository,
    pickupRepository: PickupRepository,
) : ViewModel() {

    private val requester = LocationShareRequester(viewModelScope, pickupRepository)

    /** 화면에 보여줄 최근 대상자 (최대 [MAX_VISIBLE]명, 나머지는 '전체 프로필 보기'에서) */
    val recentTargets: StateFlow<List<PickupTarget>> = repository.targets
        .map { it.take(MAX_VISIBLE) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            repository.targets.value.take(MAX_VISIBLE),
        )

    /** 위치를 요청할지 확인 팝업에 띄울 대상자. null 이면 팝업 없음 */
    val pendingConfirm: StateFlow<PickupTarget?> = combine(
        repository.targets,
        // ID 로 저장 → 화면 회전 후에도 팝업 유지
        savedStateHandle.getStateFlow<String?>(KEY_PENDING_CONFIRM, null),
    ) { targets, id -> targets.find { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** 위치 공유 요청 상태 → 대기 / 자동 취소 / 거부 팝업 */
    val shareState: StateFlow<LocationShareRequestState> = requester.state

    /** 허용 → 경유지 / 목적지 선택 화면으로, 실패 → 안내 */
    val events: Flow<LocationShareEvent> = requester.events

    init {
        // 화면에 들어올 때마다 최신 목록 불러오기
        // TODO: 서버 연동 시 로딩·실패 안내 UI (Figma 미정)
        viewModelScope.launch { repository.refresh() }
    }

    fun onTargetClick(target: PickupTarget) {
        if (requester.state.value != LocationShareRequestState.Idle) return
        savedStateHandle[KEY_PENDING_CONFIRM] = target.id
    }

    fun onConfirmDismiss() {
        savedStateHandle[KEY_PENDING_CONFIRM] = null
    }

    /** 확인 팝업의 "위치 요청하기" → 확인 팝업을 닫고 대기 팝업으로 */
    fun onConfirmRequest() {
        val target = pendingConfirm.value ?: return
        savedStateHandle[KEY_PENDING_CONFIRM] = null
        requester.request(target.name, target.phoneDigits)
    }

    /** 대기 팝업의 "요청 취소" (또는 뒤로 가기) */
    fun cancelRequest() = requester.cancel()

    /** 자동 취소 / 거부 팝업의 "확인" → 이 화면에 머묾 */
    fun dismissShareResult() = requester.dismissResult()

    companion object {
        /** Figma: 프로필 카드 3개 + 직접 입력 카드 */
        const val MAX_VISIBLE = 3

        private const val KEY_PENDING_CONFIRM = "pendingConfirmId"
    }
}
