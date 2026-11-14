package com.example.pickmeup.ui.target

import androidx.lifecycle.SavedStateHandle
import com.example.pickmeup.MainDispatcherRule
import com.example.pickmeup.data.target.FakePickupTargetRepository
import com.example.pickmeup.model.PickupTarget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PickupTargetManageViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val minsu = PickupTarget("a", "김민수", "01012345678", 2)
    private val seoyeon = PickupTarget("b", "이서연", "01098765432", 1)
    private val repository = FakePickupTargetRepository(listOf(minsu, seoyeon))

    private fun TestScope.viewModel(handle: SavedStateHandle = SavedStateHandle()) =
        PickupTargetManageViewModel(handle, repository).also { vm ->
            // 화면 구독 흉내 (WhileSubscribed)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        }

    @Test
    fun entering_showsAllSavedTargets() = runTest {
        val vm = viewModel()

        assertEquals(1, repository.refreshCount)
        assertEquals(listOf("김민수", "이서연"), vm.uiState.value.targets.map { it.name })
    }

    @Test
    fun deleteClick_opensConfirm_andDismissCloses() = runTest {
        val vm = viewModel()

        vm.onDeleteClick(minsu)
        assertEquals(minsu, vm.uiState.value.pendingDelete)

        vm.onDeleteDismiss()
        assertNull(vm.uiState.value.pendingDelete)
        assertEquals(2, vm.uiState.value.targets.size)
    }

    @Test
    fun deleteConfirm_removesTargetAndClosesConfirm() = runTest {
        val vm = viewModel()

        vm.onDeleteClick(minsu)
        vm.onDeleteConfirm()

        assertNull(vm.uiState.value.pendingDelete)
        assertEquals(listOf("이서연"), vm.uiState.value.targets.map { it.name })
        assertEquals(listOf("이서연"), repository.stored.map { it.name })
    }

    @Test
    fun openConfirm_survivesRecreation() = runTest {
        val handle = SavedStateHandle()
        viewModel(handle).onDeleteClick(seoyeon)

        val recreated = viewModel(handle)

        assertEquals(seoyeon, recreated.uiState.value.pendingDelete)
    }

    @Test
    fun editClick_opensPopupWithSavedValues() = runTest {
        val vm = viewModel()

        vm.onEditClick(minsu)

        val editing = vm.uiState.value.editing!!
        assertEquals("김민수", editing.name)
        assertEquals("01012345678", editing.phoneDigits)
        assertTrue(editing.canSave)
    }

    @Test
    fun editSave_updatesTarget_keepsOrder_andClosesPopup() = runTest {
        val vm = viewModel()

        vm.onEditClick(seoyeon)
        vm.onEditNameChange(" 이서윤 ")
        vm.onEditPhoneChange("010-5555-6666")
        vm.onEditSave()

        assertNull(vm.uiState.value.editing)
        assertEquals(listOf("김민수", "이서윤"), vm.uiState.value.targets.map { it.name })
        assertEquals("01055556666", repository.stored.last().phoneDigits)
    }

    @Test
    fun invalidInput_cannotSave() = runTest {
        val vm = viewModel()
        vm.onEditClick(minsu)

        vm.onEditNameChange("  ")
        assertFalse(vm.uiState.value.editing!!.canSave)

        vm.onEditNameChange("김민수")
        vm.onEditPhoneChange("0101234")
        assertFalse(vm.uiState.value.editing!!.canSave)

        vm.onEditSave()
        assertEquals("01012345678", repository.stored.first().phoneDigits) // 저장 안 됨
    }

    @Test
    fun editDismiss_discardsChanges() = runTest {
        val vm = viewModel()

        vm.onEditClick(minsu)
        vm.onEditNameChange("다른 이름")
        vm.onEditDismiss()

        assertNull(vm.uiState.value.editing)
        assertEquals("김민수", repository.stored.first().name)
    }

    @Test
    fun editToExistingPhone_mergesIntoOneProfile() = runTest {
        val vm = viewModel()

        vm.onEditClick(minsu)
        vm.onEditPhoneChange(seoyeon.phoneDigits)
        vm.onEditSave()

        // 같은 번호는 한 명으로 취급 → 수정한 프로필만 남음
        assertEquals(listOf("김민수"), vm.uiState.value.targets.map { it.name })
    }

    @Test
    fun openEdit_survivesRecreation() = runTest {
        val handle = SavedStateHandle()
        viewModel(handle).apply {
            onEditClick(minsu)
            onEditNameChange("김민수2")
        }

        val recreated = viewModel(handle)

        assertEquals("김민수2", recreated.uiState.value.editing?.name)
    }
}
