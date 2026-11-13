package com.example.pickmeup.ui.share

import androidx.lifecycle.SavedStateHandle
import com.example.pickmeup.MainDispatcherRule
import com.example.pickmeup.data.message.FakePickupMessageRepository
import com.example.pickmeup.data.pickup.FakePickupRepository
import com.example.pickmeup.model.PickupMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class PickupInProgressViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakePickupRepository(responseDelay = Duration.ZERO)

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) =
        PickupInProgressViewModel(handle, repository, FakePickupMessageRepository(interval = 2.seconds))

    @Test
    fun withoutActivePickup_locationIsNull() {
        assertNull(viewModel().location)
    }

    @Test
    fun complete_showsCompletedResult_butScreenKeepsLocation() = runTest {
        repository.requestLocationShare("홍길동", "01012345678")
        val vm = viewModel()

        vm.completePickup()

        assertEquals(PickupResult.Completed, vm.result.value)
        assertNull(repository.currentShare.value)
        // 결과 팝업 뒤에 픽업 화면이 그대로 있어야 함 ('데이터 없음'으로 뒤로 가지 않음)
        assertNotNull(vm.location)
    }

    @Test
    fun cancel_showsCancelledResult() = runTest {
        repository.requestLocationShare("홍길동", "01012345678")
        val vm = viewModel()

        vm.cancelPickup()

        assertEquals(PickupResult.Cancelled, vm.result.value)
        assertNull(repository.currentShare.value)
    }

    @Test
    fun afterResult_otherButtonIsIgnored() = runTest {
        repository.requestLocationShare("홍길동", "01012345678")
        val vm = viewModel()

        vm.completePickup()
        vm.cancelPickup()

        assertEquals(PickupResult.Completed, vm.result.value)
    }

    @Test
    fun result_survivesRecreation() = runTest {
        repository.requestLocationShare("홍길동", "01012345678")
        val handle = SavedStateHandle()
        viewModel(handle).cancelPickup()

        assertEquals(PickupResult.Cancelled, viewModel(handle).result.value)
    }

    @Test
    fun messages_cycleEveryTwoSeconds() = runTest {
        val vm = viewModel()
        // 화면 구독 흉내 (WhileSubscribed)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.message.collect {} }

        assertEquals(PickupMessage.GoingToPlace, vm.message.value)
        advanceTimeBy(2.seconds); runCurrent()
        assertEquals(PickupMessage.AlmostArrived, vm.message.value)
        advanceTimeBy(2.seconds); runCurrent()
        assertEquals(PickupMessage.Waiting, vm.message.value)
        advanceTimeBy(2.seconds); runCurrent()
        assertEquals(PickupMessage.GoingToPlace, vm.message.value) // 처음부터 반복
    }
}
