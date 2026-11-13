package com.example.pickmeup.ui.target

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickmeup.data.target.PickupTargetRepository
import com.example.pickmeup.model.PickupTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PickupTargetManageUiState(
    /** 저장된 대상자 전체 (최근 저장 순) */
    val targets: List<PickupTarget> = emptyList(),
    /** 삭제 확인 팝업에 띄울 대상자. null 이면 팝업 없음 */
    val pendingDelete: PickupTarget? = null,
    /** 삭제 처리 중 (팝업 버튼 중복 클릭 방지) */
    val isDeleting: Boolean = false,
)

/** 저장된 픽업 대상자 전체 목록 화면 */
@HiltViewModel
class PickupTargetManageViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: PickupTargetRepository,
) : ViewModel() {

    private val isDeleting = MutableStateFlow(false)

    val uiState: StateFlow<PickupTargetManageUiState> = combine(
        repository.targets,
        // 팝업 대상은 ID 로 저장 → 화면 회전 후에도 팝업 유지
        savedStateHandle.getStateFlow<String?>(KEY_PENDING_DELETE, null),
        isDeleting,
    ) { targets, pendingId, deleting ->
        PickupTargetManageUiState(
            targets = targets,
            pendingDelete = targets.find { it.id == pendingId },
            isDeleting = deleting,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PickupTargetManageUiState())

    init {
        // TODO: 서버 연동 시 로딩·실패 안내 UI (Figma 미정)
        viewModelScope.launch { repository.refresh() }
    }

    fun onDeleteClick(target: PickupTarget) {
        savedStateHandle[KEY_PENDING_DELETE] = target.id
    }

    fun onDeleteDismiss() {
        if (!isDeleting.value) savedStateHandle[KEY_PENDING_DELETE] = null
    }

    fun onDeleteConfirm() {
        val id = savedStateHandle.get<String>(KEY_PENDING_DELETE) ?: return
        if (isDeleting.value) return
        viewModelScope.launch {
            isDeleting.value = true
            repository.delete(id)
                .onSuccess { savedStateHandle[KEY_PENDING_DELETE] = null }
                .onFailure { /* TODO: 실패 안내 UI (Figma 미정). 팝업은 열어둬서 다시 시도 가능 */ }
            isDeleting.value = false
        }
    }

    private companion object {
        const val KEY_PENDING_DELETE = "pendingDeleteId"
    }
}
