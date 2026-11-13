package com.example.pickmeup.ui.share

import androidx.lifecycle.SavedStateHandle
import com.example.pickmeup.MainDispatcherRule
import com.example.pickmeup.data.pickup.FakePickupRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration

class PickupInProgressViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakePickupRepository(responseDelay = Duration.ZERO)

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) =
        PickupInProgressViewModel(handle, repository)

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
}
