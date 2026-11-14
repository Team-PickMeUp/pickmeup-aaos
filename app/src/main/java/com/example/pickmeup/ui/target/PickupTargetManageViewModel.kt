package com.example.pickmeup.ui.target

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickmeup.data.target.PickupTargetRepository
import com.example.pickmeup.model.PickupTarget
import com.example.pickmeup.ui.location.PhoneNumber
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
    /** 수정 팝업 (Figma 2-1-3). null 이면 팝업 없음 */
    val editing: ProfileEditState? = null,
)

/** 프로필 수정 팝업 입력값 */
data class ProfileEditState(
    val target: PickupTarget,
    val name: String,
    /** 숫자만 (화면에는 하이픈 넣어서 표시) */
    val phoneDigits: String,
    /** 저장 처리 중 (버튼 중복 클릭 방지) */
    val isSaving: Boolean = false,
) {
    val canSave: Boolean
        get() = !isSaving && name.isNotBlank() && PhoneNumber.isValidMobile(phoneDigits)
}

/** SavedStateHandle 에 저장하는 수정 팝업 입력값 (화면 회전 후에도 유지) */
private data class EditInput(val id: String?, val name: String, val phoneDigits: String, val isSaving: Boolean)

/** 저장된 픽업 대상자 전체 목록 화면 */
@HiltViewModel
class PickupTargetManageViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: PickupTargetRepository,
) : ViewModel() {

    private val isDeleting = MutableStateFlow(false)
    private val isSaving = MutableStateFlow(false)

    private val editInput = combine(
        savedStateHandle.getStateFlow<String?>(KEY_EDIT_ID, null),
        savedStateHandle.getStateFlow(KEY_EDIT_NAME, ""),
        savedStateHandle.getStateFlow(KEY_EDIT_PHONE, ""),
        isSaving,
        ::EditInput,
    )

    val uiState: StateFlow<PickupTargetManageUiState> = combine(
        repository.targets,
        // 팝업 대상은 ID 로 저장 → 화면 회전 후에도 팝업 유지
        savedStateHandle.getStateFlow<String?>(KEY_PENDING_DELETE, null),
        isDeleting,
        editInput,
    ) { targets, pendingId, deleting, edit ->
        PickupTargetManageUiState(
            targets = targets,
            pendingDelete = targets.find { it.id == pendingId },
            isDeleting = deleting,
            editing = targets.find { it.id == edit.id }?.let { target ->
                ProfileEditState(target, edit.name, edit.phoneDigits, edit.isSaving)
            },
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

    /** "수정" → 저장된 이름·전화번호를 채운 수정 팝업 */
    fun onEditClick(target: PickupTarget) {
        savedStateHandle[KEY_EDIT_NAME] = target.name
        savedStateHandle[KEY_EDIT_PHONE] = target.phoneDigits
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

    /** "저장하기" → 저장되면 팝업 닫힘, 목록 순서는 그대로 */
    fun onEditSave() {
        val edit = uiState.value.editing ?: return
        if (!edit.canSave) return
        viewModelScope.launch {
            isSaving.value = true
            repository.update(edit.target.id, edit.name.trim(), edit.phoneDigits)
                .onSuccess { clearEdit() }
                .onFailure { /* TODO: 실패 안내 UI (Figma 미정). 팝업은 열어둬서 다시 시도 가능 */ }
            isSaving.value = false
        }
    }

    private fun clearEdit() {
        savedStateHandle[KEY_EDIT_ID] = null
        savedStateHandle[KEY_EDIT_NAME] = ""
        savedStateHandle[KEY_EDIT_PHONE] = ""
    }

    private companion object {
        const val KEY_PENDING_DELETE = "pendingDeleteId"
        const val KEY_EDIT_ID = "editId"
        const val KEY_EDIT_NAME = "editName"
        const val KEY_EDIT_PHONE = "editPhoneDigits"
    }
}
