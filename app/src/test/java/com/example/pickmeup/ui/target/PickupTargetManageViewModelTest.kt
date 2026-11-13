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
import org.junit.Assert.assertNull
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
}
