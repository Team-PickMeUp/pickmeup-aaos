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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PickupTargetSelectViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun target(name: String, savedAt: Long) =
        PickupTarget(id = name, name = name, phoneDigits = "0101234567$savedAt", savedAt = savedAt)

    private fun viewModel(
        targetRepository: FakePickupTargetRepository,
        pickupRepository: FakePickupRepository = FakePickupRepository(Duration.ZERO),
        handle: SavedStateHandle = SavedStateHandle(),
    ) = PickupTargetSelectViewModel(handle, targetRepository, pickupRepository)

    /** 화면이 구독 중인 상태를 흉내 (WhileSubscribed 라 구독자가 있어야 값이 갱신됨) */
    private fun TestScope.subscribe(vm: PickupTargetSelectViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.recentTargets.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.pendingConfirm.collect {} }
    }

    private fun TestScope.collectEvents(vm: PickupTargetSelectViewModel): List<LocationShareEvent> {
        val events = mutableListOf<LocationShareEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.collect { events += it } }
        return events
    }

    @Test
    fun entering_refreshesAndShowsTargets() = runTest {
        val repository = FakePickupTargetRepository(listOf(target("가", 1)))

        val vm = viewModel(repository)
        subscribe(vm)

        assertEquals(1, repository.refreshCount)
        assertEquals(listOf("가"), vm.recentTargets.value.map { it.name })
    }

    @Test
    fun noSavedTargets_showsNothing() = runTest {
        val vm = viewModel(FakePickupTargetRepository(emptyList()))
        subscribe(vm)

        assertTrue(vm.recentTargets.value.isEmpty())
    }

    @Test
    fun moreThanThreeTargets_showsOnlyRecentThree() = runTest {
        val saved = listOf(target("가", 5), target("나", 4), target("다", 3), target("라", 2))
        val vm = viewModel(FakePickupTargetRepository(saved))
        subscribe(vm)

        assertEquals(listOf("가", "나", "다"), vm.recentTargets.value.map { it.name })
    }

    @Test
    fun targetClick_opensConfirm_andDismissCloses() = runTest {
        val ga = target("가", 1)
        val vm = viewModel(FakePickupTargetRepository(listOf(ga)))
        subscribe(vm)

        vm.onTargetClick(ga)
        assertEquals(ga, vm.pendingConfirm.value)

        vm.onConfirmDismiss()
        assertNull(vm.pendingConfirm.value)
        assertEquals(LocationShareRequestState.Idle, vm.shareState.value)
    }

    @Test
    fun confirmRequest_requestsSelectedTarget_andSendsAllowed() = runTest {
        val ga = target("가", 1)
        val pickupRepository = FakePickupRepository(Duration.ZERO)
        val vm = viewModel(FakePickupTargetRepository(listOf(ga)), pickupRepository)
        subscribe(vm)
        val events = collectEvents(vm)

        vm.onTargetClick(ga)
        vm.onConfirmRequest()

        assertNull(vm.pendingConfirm.value)
        assertEquals(listOf(LocationShareEvent.Allowed), events)
        assertEquals("가", pickupRepository.currentShare.value?.targetName)
    }

    @Test
    fun confirmRequest_showsWaitingOnThisScreen_untilCancelled() = runTest {
        val ga = target("가", 1)
        val vm = viewModel(FakePickupTargetRepository(listOf(ga)), FakePickupRepository(10.minutes))
        subscribe(vm)

        vm.onTargetClick(ga)
        vm.onConfirmRequest()
        assertEquals(LocationShareRequestState.Waiting(300), vm.shareState.value)

        vm.cancelRequest()
        assertEquals(LocationShareRequestState.Idle, vm.shareState.value)
    }

    @Test
    fun denied_showsDeniedPopup_thenStaysOnScreen() = runTest {
        val ga = target("가", 1)
        val vm = viewModel(FakePickupTargetRepository(listOf(ga)), FakePickupRepository(Duration.ZERO, denyRequests = true))
        subscribe(vm)
        val events = collectEvents(vm)

        vm.onTargetClick(ga)
        vm.onConfirmRequest()
        assertEquals(LocationShareRequestState.Denied, vm.shareState.value)

        vm.dismissShareResult()
        assertEquals(LocationShareRequestState.Idle, vm.shareState.value)
        assertTrue(events.isEmpty())
    }

    @Test
    fun openConfirm_survivesRecreation() = runTest {
        val ga = target("가", 1)
        val repository = FakePickupTargetRepository(listOf(ga))
        val handle = SavedStateHandle()
        viewModel(repository, handle = handle).onTargetClick(ga)

        val recreated = viewModel(repository, handle = handle)
        subscribe(recreated)

        assertEquals(ga, recreated.pendingConfirm.value)
    }
}
