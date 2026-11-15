package com.example.pickmeup.ui.target

import androidx.lifecycle.SavedStateHandle
import com.example.pickmeup.MainDispatcherRule
import com.example.pickmeup.data.pickup.FakePickupRepository
import com.example.pickmeup.data.target.FakePickupTargetRepository
import com.example.pickmeup.model.PickupTarget
import com.example.pickmeup.ui.location.LocationShareEvent
import com.example.pickmeup.ui.location.LocationShareRequestState
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class PickupTargetManageViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val minsu = PickupTarget("a", "김민수", "01012345678", 2)
    private val seoyeon = PickupTarget("b", "이서연", "01098765432", 1)
    private val repository = FakePickupTargetRepository(listOf(minsu, seoyeon))

    private fun TestScope.viewModel(
        handle: SavedStateHandle = SavedStateHandle(),
        pickupRepository: FakePickupRepository = FakePickupRepository(Duration.ZERO),
    ) =
        PickupTargetManageViewModel(handle, repository, pickupRepository).also { vm ->
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

    private fun TestScope.collectEvents(vm: PickupTargetManageViewModel): List<LocationShareEvent> {
        val events = mutableListOf<LocationShareEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.collect { events += it } }
        return events
    }

    // ---------- 줄 선택 → 바로 위치 요청 (2-1-1 → 2-1-5) ----------

    @Test
    fun targetClick_opensRequestConfirm_andDismissCloses() = runTest {
        val vm = viewModel()

        vm.onTargetClick(seoyeon)
        assertEquals(seoyeon, vm.uiState.value.pendingConfirm)

        vm.onConfirmDismiss()
        assertNull(vm.uiState.value.pendingConfirm)
    }

    @Test
    fun confirmRequest_closesConfirm_andAllowedEmitsEvent() = runTest {
        val vm = viewModel()
        val events = collectEvents(vm)

        vm.onTargetClick(minsu)
        vm.onConfirmRequest()

        assertNull(vm.uiState.value.pendingConfirm)
        assertEquals(listOf(LocationShareEvent.Allowed), events)
    }

    @Test
    fun whileWaiting_otherPopupsDoNotOpen() = runTest {
        // 응답이 오지 않는 요청 → 대기 팝업 유지
        val vm = viewModel(pickupRepository = FakePickupRepository(10.minutes))

        vm.onTargetClick(minsu)
        vm.onConfirmRequest()
        assertTrue(vm.shareState.value is LocationShareRequestState.Waiting)

        vm.onTargetClick(seoyeon)
        vm.onAddClick()
        vm.onEditClick(seoyeon)
        vm.onDeleteClick(seoyeon)

        val state = vm.uiState.value
        assertNull(state.pendingConfirm)
        assertNull(state.editing)
        assertNull(state.pendingDelete)

        vm.cancelRequest()
        assertEquals(LocationShareRequestState.Idle, vm.shareState.value)
    }

    // ---------- 추가 (2-1-1-1 → 2-1-1-2) ----------

    @Test
    fun addClick_opensEmptyAddPopup() = runTest {
        val vm = viewModel()

        vm.onAddClick()

        val editing = vm.uiState.value.editing!!
        assertTrue(editing.isNew)
        assertEquals("", editing.name)
        assertEquals("", editing.phoneDigits)
        assertFalse(editing.canSave)
    }

    @Test
    fun addSave_addsTargetOnTop_andOpensRequestConfirm() = runTest {
        val vm = viewModel()

        vm.onAddClick()
        vm.onEditNameChange(" 박지훈 ")
        vm.onEditPhoneChange("010-5555-6666")
        vm.onEditSave()

        val state = vm.uiState.value
        assertNull(state.editing)
        assertEquals(listOf("박지훈", "김민수", "이서연"), state.targets.map { it.name })
        assertEquals("박지훈", state.pendingConfirm?.name)
        assertEquals("01055556666", state.pendingConfirm?.phoneDigits)
    }

    @Test
    fun addDismiss_addsNothing() = runTest {
        val vm = viewModel()

        vm.onAddClick()
        vm.onEditNameChange("박지훈")
        vm.onEditDismiss()

        assertNull(vm.uiState.value.editing)
        assertEquals(2, repository.stored.size)
    }

    @Test
    fun openAdd_survivesRecreation() = runTest {
        val handle = SavedStateHandle()
        viewModel(handle).apply {
            onAddClick()
            onEditNameChange("박지")
        }

        val recreated = viewModel(handle)

        assertTrue(recreated.uiState.value.editing!!.isNew)
        assertEquals("박지", recreated.uiState.value.editing?.name)
    }
}
