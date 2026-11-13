package com.example.pickmeup.ui.location

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickmeup.data.pickup.PickupRepository
import com.example.pickmeup.data.target.PickupTargetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationRequestUiState(
    val name: String = "",
    /** 숫자만 저장 (화면에는 하이픈 넣어서 표시) */
    val phoneDigits: String = "",
    /** "다음 픽업을 위해 대상자 저장" 체크 여부 */
    val saveForNext: Boolean = false,
    /** 대상자 응답을 기다리는 중 → 대기 팝업 표시, 버튼 중복 클릭 방지 */
    val isRequesting: Boolean = false,
) {
    val canRequest: Boolean
        get() = !isRequesting && name.isNotBlank() && PhoneNumber.isValidMobile(phoneDigits)
}

sealed interface LocationRequestEvent {
    /** 대상자 응답을 받음 → 공유 결과 화면으로 */
    data object ResponseReceived : LocationRequestEvent

    // TODO: 실패 안내 UI (Figma 미정, Pleos 에서는 Toast 가 보이지 않음)
    data class RequestFailed(val cause: Throwable) : LocationRequestEvent
}

@HiltViewModel
class LocationRequestViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val pickupRepository: PickupRepository,
    private val targetRepository: PickupTargetRepository,
) : ViewModel() {

    private val isRequesting = MutableStateFlow(false)

    /** 응답을 기다리는 중인 요청 (취소용) */
    private var requestJob: Job? = null

    // 입력값은 SavedStateHandle 에 저장 → 다른 화면에 갔다 와도, 화면 회전 후에도 유지
    val uiState: StateFlow<LocationRequestUiState> = combine(
        savedStateHandle.getStateFlow(KEY_NAME, ""),
        savedStateHandle.getStateFlow(KEY_PHONE, ""),
        savedStateHandle.getStateFlow(KEY_SAVE, false),
        isRequesting,
    ) { name, phone, save, requesting ->
        LocationRequestUiState(name, phone, save, requesting)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, LocationRequestUiState())

    private val _events = Channel<LocationRequestEvent>(Channel.BUFFERED)
    val events: Flow<LocationRequestEvent> = _events.receiveAsFlow()

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
        if (isRequesting.value || name.isEmpty() || !PhoneNumber.isValidMobile(phone)) return

        isRequesting.value = true
        requestJob = viewModelScope.launch {
            try {
                pickupRepository.requestLocationShare(name, phone)
                    .onSuccess {
                        if (savedStateHandle.get<Boolean>(KEY_SAVE) == true) {
                            // 저장 실패는 위치 요청 흐름을 막지 않음 (다음에 다시 저장하면 됨)
                            targetRepository.save(name, phone)
                        }
                        _events.send(LocationRequestEvent.ResponseReceived)
                    }
                    .onFailure { _events.send(LocationRequestEvent.RequestFailed(it)) }
            } finally {
                // 응답·실패·취소 어느 경우든 대기 팝업 닫기
                isRequesting.value = false
                requestJob = null
            }
        }
    }

    /**
     * 대기 팝업의 "요청 취소" (또는 뒤로 가기).
     * 응답 기다리기를 멈추고 서버에 취소를 알림 → 입력값은 그대로 두고 위치 요청 화면에 머묾
     */
    fun cancelRequest() {
        val job = requestJob ?: return
        job.cancel()
        viewModelScope.launch {
            // TODO: 취소 실패 안내 UI (Figma 미정)
            pickupRepository.cancelLocationRequest()
        }
    }

    private companion object {
        const val KEY_NAME = "name"
        const val KEY_PHONE = "phoneDigits"
        const val KEY_SAVE = "saveForNext"
    }
}
