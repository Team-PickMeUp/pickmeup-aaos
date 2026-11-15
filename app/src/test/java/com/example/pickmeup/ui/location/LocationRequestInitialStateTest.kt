package com.example.pickmeup.ui.location

import androidx.lifecycle.SavedStateHandle
import com.example.pickmeup.MainDispatcherRule
import com.example.pickmeup.data.pickup.FakePickupRepository
import com.example.pickmeup.data.target.FakePickupTargetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration

/**
 * 위치 요청 화면의 이름 입력란은 첫 화면의 이름 값으로 입력 상태를 만들기 때문에
 * ViewModel 의 **초기값**(아직 아무 코루틴도 돌지 않은 시점)에 미리 채운 값이 들어 있어야 함.
 *
 * Main 을 StandardTestDispatcher 로 두고 시간을 진행하지 않아서 stateIn 의 초기값만 확인합니다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocationRequestInitialStateTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun prefilledArgs_areInInitialState_beforeAnyCollection() {
        val vm = LocationRequestViewModel(
            SavedStateHandle(mapOf("name" to "김민수", "phoneDigits" to "01012345678", "saveForNext" to true)),
            FakePickupRepository(responseDelay = Duration.ZERO),
            FakePickupTargetRepository(),
        )

        val initial = vm.uiState.value
        assertEquals("김민수", initial.name)
        assertEquals("01012345678", initial.phoneDigits)
        assertTrue(initial.saveForNext)
    }
}
