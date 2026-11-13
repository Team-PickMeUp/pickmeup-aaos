package com.example.pickmeup.ui.share

import com.example.pickmeup.MainDispatcherRule
import com.example.pickmeup.data.pickup.FakePickupRepository
import kotlinx.coroutines.flow.first
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

    @Test
    fun withoutActivePickup_locationIsNull() {
        assertNull(PickupInProgressViewModel(repository).location)
    }

    @Test
    fun complete_clearsPickup_butScreenKeepsLocation() = runTest {
        repository.requestLocationShare("홍길동", "01012345678")
        val vm = PickupInProgressViewModel(repository)

        vm.completePickup()

        assertEquals(Unit, vm.finished.first())
        assertNull(repository.currentShare.value)
        // 종료 이벤트로 화면이 닫히기 전에 '데이터 없음' 처리(뒤로 가기)가 일어나지 않아야 함
        assertNotNull(vm.location)
    }

    @Test
    fun cancel_clearsPickupAndFinishes() = runTest {
        repository.requestLocationShare("홍길동", "01012345678")
        val vm = PickupInProgressViewModel(repository)

        vm.cancelPickup()

        assertEquals(Unit, vm.finished.first())
        assertNull(repository.currentShare.value)
    }
}
