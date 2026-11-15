package com.example.pickmeup.ui.location

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickmeup.data.pickup.PickupRepository
import com.example.pickmeup.data.target.PickupTargetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class LocationRequestUiState(
    val name: String = "",
    /** 숫자만 저장 (화면에는 하이픈 넣어서 표시) */
    val phoneDigits: String = "",
    /** "다음 픽업을 위해 대상자 저장" 체크 여부 */
    val saveForNext: Boolean = false,
    /** 위치 공유 요청 상태 → 대기 / 자동 취소 / 거부 팝업 */
    val shareState: LocationShareRequestState = LocationShareRequestState.Idle,
) {
    /** 대상자 응답을 기다리는 중 (버튼 중복 클릭 방지) */
    val isRequesting: Boolean
        get() = shareState is LocationShareRequestState.Waiting

    val canRequest: Boolean
        get() = shareState == LocationShareRequestState.Idle &&
            name.isNotBlank() && PhoneNumber.isValidMobile(phoneDigits)
}

@HiltViewModel
class LocationRequestViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    pickupRepository: PickupRepository,
    private val targetRepository: PickupTargetRepository,
) : ViewModel() {

    private val requester = LocationShareRequester(viewModelScope, pickupRepository)

    // 입력값은 SavedStateHandle 에 저장 → 다른 화면에 갔다 와도, 화면 회전 후에도 유지
    val uiState: StateFlow<LocationRequestUiState> = combine(
        savedStateHandle.getStateFlow(KEY_NAME, ""),
        savedStateHandle.getStateFlow(KEY_PHONE, ""),
        savedStateHandle.getStateFlow(KEY_SAVE, false),
        requester.state,
    ) { name, phone, save, shareState ->
        LocationRequestUiState(name, phone, save, shareState)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        // 첫 화면부터 저장된 값이 보이도록 초기값도 SavedStateHandle 에서 바로 읽음
        // (이름 입력란은 첫 값으로 입력 상태를 만들기 때문에 빈 값으로 시작하면 미리 채운 이름이 사라짐)
        LocationRequestUiState(
            name = savedStateHandle[KEY_NAME] ?: "",
            phoneDigits = savedStateHandle[KEY_PHONE] ?: "",
            saveForNext = savedStateHandle[KEY_SAVE] ?: false,
        ),
    )

    /** 허용 → 경유지 / 목적지 선택 화면으로, 실패 → 안내 */
    val events: Flow<LocationShareEvent> = requester.events

    fun onNameChange(name: String) {
        savedStateHandle[KEY_NAME] = name
    }

    fun onPhoneChange(input: String) {
        savedStateHandle[KEY_PHONE] = PhoneNumber.digitsOf(input)
    }

    fun onSaveForNextChange(checked: Boolean) {
        savedStateHandle[KEY_SAVE] = checked
    }

    fun requestLocation() {
        val name = savedStateHandle.get<String>(KEY_NAME).orEmpty().trim()
        val phone = savedStateHandle.get<String>(KEY_PHONE).orEmpty()
        if (name.isEmpty() || !PhoneNumber.isValidMobile(phone)) return

        requester.request(name, phone) {
            if (savedStateHandle.get<Boolean>(KEY_SAVE) == true) {
                // 저장 실패는 위치 요청 흐름을 막지 않음 (다음에 다시 저장하면 됨)
                targetRepository.save(name, phone)
            }
        }
    }

    /**
     * 대기 팝업의 "요청 취소" (또는 뒤로 가기).
     * 입력값은 그대로 두고 위치 요청 화면에 머묾
     */
    fun cancelRequest() = requester.cancel()

    /** 자동 취소 / 거부 팝업의 "확인" → 입력값 그대로 두고 이 화면에 머묾 */
    fun dismissShareResult() = requester.dismissResult()

    private companion object {
        const val KEY_NAME = "name"
        const val KEY_PHONE = "phoneDigits"
        const val KEY_SAVE = "saveForNext"
    }
}
