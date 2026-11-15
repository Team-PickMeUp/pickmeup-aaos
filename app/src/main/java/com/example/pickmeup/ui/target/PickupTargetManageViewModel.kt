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
import com.example.pickmeup.ui.location.PhoneNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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
    /** 추가(Figma 2-1-1-1) / 수정(Figma 2-1-3) 팝업. null 이면 팝업 없음 */
    val editing: ProfileEditState? = null,
    /** 위치를 요청할지 확인 팝업(Figma 2-1-1-2)에 띄울 대상자. null 이면 팝업 없음 */
    val pendingConfirm: PickupTarget? = null,
)

/** 프로필 추가 · 수정 팝업 입력값 */
data class ProfileEditState(
    /** 수정할 대상자. null 이면 새 대상자 추가 */
    val target: PickupTarget?,
    val name: String,
    /** 숫자만 (화면에는 하이픈 넣어서 표시) */
    val phoneDigits: String,
    /** 저장 처리 중 (버튼 중복 클릭 방지) */
    val isSaving: Boolean = false,
) {
    val isNew: Boolean
        get() = target == null

    val canSave: Boolean
        get() = !isSaving && name.isNotBlank() && PhoneNumber.isValidMobile(phoneDigits)
}

/** SavedStateHandle 에 저장하는 추가 · 수정 팝업 입력값 (화면 회전 후에도 유지) */
private data class EditInput(
    val adding: Boolean,
    val id: String?,
    val name: String,
    val phoneDigits: String,
    val isSaving: Boolean,
)

/**
 * 저장된 픽업 대상자 전체 목록 화면.
 * - 대상자 줄 선택 → 확인 팝업 → 이 화면에서 바로 위치 요청 + 대기 팝업 (Figma 2-1-5)
 * - "+ 추가" → 입력 팝업 → 저장하면 목록에 추가하고 바로 확인 팝업 (Figma 2-1-1-1 → 2-1-1-2)
 */
@HiltViewModel
class PickupTargetManageViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: PickupTargetRepository,
    pickupRepository: PickupRepository,
) : ViewModel() {

    private val requester = LocationShareRequester(viewModelScope, pickupRepository)

    private val isDeleting = MutableStateFlow(false)
    private val isSaving = MutableStateFlow(false)

    private val editInput = combine(
        savedStateHandle.getStateFlow(KEY_ADDING, false),
        savedStateHandle.getStateFlow<String?>(KEY_EDIT_ID, null),
        savedStateHandle.getStateFlow(KEY_EDIT_NAME, ""),
        savedStateHandle.getStateFlow(KEY_EDIT_PHONE, ""),
        isSaving,
        ::EditInput,
    )

    // 팝업 대상은 ID 로 저장 → 화면 회전 후에도 팝업 유지
    private val popupIds = combine(
        savedStateHandle.getStateFlow<String?>(KEY_PENDING_DELETE, null),
        savedStateHandle.getStateFlow<String?>(KEY_PENDING_CONFIRM, null),
        ::Pair,
    )

    val uiState: StateFlow<PickupTargetManageUiState> = combine(
        repository.targets,
        popupIds,
        isDeleting,
        editInput,
    ) { targets, (deleteId, confirmId), deleting, edit ->
        PickupTargetManageUiState(
            targets = targets,
            pendingDelete = targets.find { it.id == deleteId },
            isDeleting = deleting,
            editing = if (edit.adding) {
                ProfileEditState(null, edit.name, edit.phoneDigits, edit.isSaving)
            } else {
                targets.find { it.id == edit.id }?.let { target ->
                    ProfileEditState(target, edit.name, edit.phoneDigits, edit.isSaving)
                }
            },
            pendingConfirm = targets.find { it.id == confirmId },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PickupTargetManageUiState())

    /** 위치 공유 요청 상태 → 대기 / 자동 취소 / 거부 팝업 */
    val shareState: StateFlow<LocationShareRequestState> = requester.state

    /** 허용 → 경유지 / 목적지 선택 화면으로, 실패 → 안내 */
    val events: Flow<LocationShareEvent> = requester.events

    init {
        // TODO: 서버 연동 시 로딩·실패 안내 UI (Figma 미정)
        viewModelScope.launch { repository.refresh() }
    }

    /** 위치 요청 중(대기 · 결과 팝업)에는 다른 팝업을 열지 않음 */
    private val isRequesting: Boolean
        get() = requester.state.value != LocationShareRequestState.Idle

    // ---------- 위치 요청 ----------

    /** 대상자 줄 선택 → 위치를 요청할지 확인 팝업 */
    fun onTargetClick(target: PickupTarget) {
        if (isRequesting) return
        savedStateHandle[KEY_PENDING_CONFIRM] = target.id
    }

    fun onConfirmDismiss() {
        savedStateHandle[KEY_PENDING_CONFIRM] = null
    }

    /** 확인 팝업의 "위치 요청하기" → 확인 팝업을 닫고 대기 팝업으로 */
    fun onConfirmRequest() {
        val target = uiState.value.pendingConfirm ?: return
        savedStateHandle[KEY_PENDING_CONFIRM] = null
        requester.request(target.name, target.phoneDigits)
    }

    /** 대기 팝업의 "요청 취소" (또는 뒤로 가기) */
    fun cancelRequest() = requester.cancel()

    /** 자동 취소 / 거부 팝업의 "확인" → 이 화면에 머묾 */
    fun dismissShareResult() = requester.dismissResult()

    // ---------- 삭제 ----------

    fun onDeleteClick(target: PickupTarget) {
        if (isRequesting) return
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

    // ---------- 추가 · 수정 ----------

    /** "+ 추가" → 빈 입력 팝업 */
    fun onAddClick() {
        if (isRequesting) return
        savedStateHandle[KEY_EDIT_NAME] = ""
        savedStateHandle[KEY_EDIT_PHONE] = ""
        savedStateHandle[KEY_EDIT_ID] = null
        savedStateHandle[KEY_ADDING] = true
    }

    /** "수정" → 저장된 이름·전화번호를 채운 수정 팝업 */
    fun onEditClick(target: PickupTarget) {
        if (isRequesting) return
        savedStateHandle[KEY_EDIT_NAME] = target.name
        savedStateHandle[KEY_EDIT_PHONE] = target.phoneDigits
        savedStateHandle[KEY_ADDING] = false
        savedStateHandle[KEY_EDIT_ID] = target.id
    }

    fun onEditNameChange(name: String) {
        savedStateHandle[KEY_EDIT_NAME] = name
    }

    fun onEditPhoneChange(input: String) {
        savedStateHandle[KEY_EDIT_PHONE] = PhoneNumber.digitsOf(input)
    }

    fun onEditDismiss() {
        if (!isSaving.value) clearEdit()
    }

    /**
     * "저장하기"
     * - 수정: 저장되면 팝업 닫힘, 목록 순서는 그대로
     * - 추가: 목록 맨 위에 추가하고 바로 위치 요청 확인 팝업 (같은 번호가 있으면 새 정보로 바뀜)
     */
    fun onEditSave() {
        val edit = uiState.value.editing ?: return
        if (!edit.canSave) return
        val name = edit.name.trim()
        viewModelScope.launch {
            isSaving.value = true
            if (edit.target == null) {
                repository.save(name, edit.phoneDigits)
                    .onSuccess { saved ->
                        clearEdit()
                        savedStateHandle[KEY_PENDING_CONFIRM] = saved.id
                    }
                    .onFailure { /* TODO: 실패 안내 UI (Figma 미정). 팝업은 열어둬서 다시 시도 가능 */ }
            } else {
                repository.update(edit.target.id, name, edit.phoneDigits)
                    .onSuccess { clearEdit() }
                    .onFailure { /* TODO: 실패 안내 UI (Figma 미정). 팝업은 열어둬서 다시 시도 가능 */ }
            }
            isSaving.value = false
        }
    }

    private fun clearEdit() {
        savedStateHandle[KEY_ADDING] = false
        savedStateHandle[KEY_EDIT_ID] = null
        savedStateHandle[KEY_EDIT_NAME] = ""
        savedStateHandle[KEY_EDIT_PHONE] = ""
    }

    private companion object {
        const val KEY_PENDING_DELETE = "pendingDeleteId"
        const val KEY_PENDING_CONFIRM = "pendingConfirmId"
        const val KEY_ADDING = "adding"
        const val KEY_EDIT_ID = "editId"
        const val KEY_EDIT_NAME = "editName"
        const val KEY_EDIT_PHONE = "editPhoneDigits"
    }
}
