package com.example.pickmeup.ui.location

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickmeup.data.pickup.PickupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    /** 요청 중에는 버튼을 막아 중복 요청 방지 */
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
) : ViewModel() {

    private val isRequesting = MutableStateFlow(false)

    // 입력값은 SavedStateHandle 에 저장 → 다른 화면에 갔다 와도, 화면 회전 후에도 유지
    val uiState: StateFlow<LocationRequestUiState> = combine(
        savedStateHandle.getStateFlow(KEY_NAME, ""),
        savedStateHandle.getStateFlow(KEY_PHONE, ""),
        isRequesting,
    ) { name, phone, requesting ->
        LocationRequestUiState(name, phone, requesting)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, LocationRequestUiState())

    private val _events = Channel<LocationRequestEvent>(Channel.BUFFERED)
    val events: Flow<LocationRequestEvent> = _events.receiveAsFlow()

    fun onNameChange(name: String) {
        savedStateHandle[KEY_NAME] = name
    }

    fun onPhoneChange(input: String) {
        savedStateHandle[KEY_PHONE] = PhoneNumber.digitsOf(input)
    }

    fun requestLocation() {
        val name = savedStateHandle.get<String>(KEY_NAME).orEmpty().trim()
        val phone = savedStateHandle.get<String>(KEY_PHONE).orEmpty()
        if (isRequesting.value || name.isEmpty() || !PhoneNumber.isValidMobile(phone)) return

        viewModelScope.launch {
            isRequesting.value = true
            pickupRepository.requestLocationShare(name, phone)
                .onSuccess { _events.send(LocationRequestEvent.ResponseReceived) }
                .onFailure { _events.send(LocationRequestEvent.RequestFailed(it)) }
            isRequesting.value = false
        }
    }

    private companion object {
        const val KEY_NAME = "name"
        const val KEY_PHONE = "phoneDigits"
    }
}
