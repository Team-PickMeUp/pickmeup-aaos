package com.example.pickmeup.ui.terms

import androidx.lifecycle.SavedStateHandle
import com.example.pickmeup.MainDispatcherRule
import com.example.pickmeup.data.terms.TermsConsentRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TermsAgreementViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeConsentRepository : TermsConsentRepository {
        var saved = false
        override fun hasAgreedToCurrentTerms() = saved
        override fun saveAgreement(nowMillis: Long) {
            saved = true
        }
    }

    private val repository = FakeConsentRepository()
    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) =
        TermsAgreementViewModel(handle, repository)

    @Test
    fun startsUnchecked_andCannotAgree() {
        val vm = viewModel()

        assertTrue(vm.uiState.value.checked.isEmpty())
        assertFalse(vm.agree())
        assertFalse(repository.saved)
    }

    @Test
    fun checkingAllItemsOneByOne_enablesAgree() {
        val vm = viewModel()

        TermsType.entries.forEach { vm.onCheckedChange(it, true) }

        assertTrue(vm.uiState.value.allChecked)
        assertTrue(vm.agree())
        assertTrue(repository.saved)
    }

    @Test
    fun checkAll_thenUncheckOne_disablesAgree() {
        val vm = viewModel()

        vm.onCheckAllChange(true)
        vm.onCheckedChange(TermsType.entries.first(), false)

        assertFalse(vm.uiState.value.allChecked)
        assertFalse(vm.agree())
    }

    @Test
    fun checkedState_survivesRecreationWithSameSavedState() {
        val handle = SavedStateHandle()
        viewModel(handle).onCheckedChange(TermsType.Privacy, true)

        val recreated = viewModel(handle)

        assertEquals(setOf(TermsType.Privacy), recreated.uiState.value.checked)
    }
}
