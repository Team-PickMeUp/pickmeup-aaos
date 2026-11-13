package com.example.pickmeup.ui.terms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickmeup.data.terms.TermsConsentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TermsAgreementUiState(
    val checked: Set<TermsType> = emptySet(),
) {
    /** 모든 항목이 필수라서 전부 체크해야 시작할 수 있음 */
    val allChecked: Boolean get() = checked.containsAll(TermsType.entries)
}

@HiltViewModel
class TermsAgreementViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val consentRepository: TermsConsentRepository,
) : ViewModel() {

    // 체크 상태는 화면 회전·프로세스 재시작 후에도 유지 (enum 이름 목록으로 저장)
    val uiState: StateFlow<TermsAgreementUiState> = savedStateHandle
        .getStateFlow(KEY_CHECKED, arrayListOf<String>())
        .map { names -> TermsAgreementUiState(names.map(TermsType::valueOf).toSet()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, TermsAgreementUiState(savedChecked()))

    fun onCheckedChange(type: TermsType, checked: Boolean) {
        val current = savedChecked()
        save(if (checked) current + type else current - type)
    }

    fun onCheckAllChange(checked: Boolean) {
        save(if (checked) TermsType.entries.toSet() else emptySet())
    }

    /** 동의 저장. 모두 체크되지 않았으면 false */
    fun agree(): Boolean {
        if (!savedChecked().containsAll(TermsType.entries)) return false
        consentRepository.saveAgreement()
        return true
    }

    private fun savedChecked(): Set<TermsType> =
        savedStateHandle.get<List<String>>(KEY_CHECKED).orEmpty().map(TermsType::valueOf).toSet()

    private fun save(checked: Set<TermsType>) {
        savedStateHandle[KEY_CHECKED] = ArrayList(checked.map { it.name })
    }

    private companion object {
        const val KEY_CHECKED = "checked"
    }
}
