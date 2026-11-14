package com.example.pickmeup.ui.location

import androidx.lifecycle.SavedStateHandle
import com.example.pickmeup.MainDispatcherRule
import com.example.pickmeup.data.pickup.FakePickupRepository
import com.example.pickmeup.data.target.FakePickupTargetRepository
import com.example.pickmeup.model.LocationShareResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class LocationRequestViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakePickupRepository(responseDelay = Duration.ZERO)
    private val targetRepository = FakePickupTargetRepository()
    // lazy: MainDispatcherRule 이 Main 을 바꾼 뒤에 생성
    private val vm by lazy { LocationRequestViewModel(SavedStateHandle(), repository, targetRepository) }

    @Test
    fun selectedTargetArgs_prefillInputs() {
        // 픽업 불러오기에서 대상자를 고르면 화면 경로 인자(name, phoneDigits)가 SavedStateHandle 로 들어옴
        val prefilled = LocationRequestViewModel(
            SavedStateHandle(mapOf("name" to "김민수", "phoneDigits" to "01012345678")),
            repository,
            targetRepository,
        )

        assertEquals("김민수", prefilled.uiState.value.name)
        assertEquals("01012345678", prefilled.uiState.value.phoneDigits)
        assertTrue(prefilled.uiState.value.canRequest)
    }

    @Test
    fun phoneInput_isStoredAsDigitsOnly() {
        vm.onPhoneChange("010-1234-5678")

        assertEquals("01012345678", vm.uiState.value.phoneDigits)
    }

    @Test
    fun canRequest_onlyWithNameAndValidPhone() {
        assertFalse(vm.uiState.value.canRequest)

        vm.onNameChange("홍길동")
        assertFalse(vm.uiState.value.canRequest)

        vm.onPhoneChange("0101234")
        assertFalse(vm.uiState.value.canRequest)

        vm.onPhoneChange("01012345678")
        assertTrue(vm.uiState.value.canRequest)
    }

    @Test
    fun blankName_isNotRequested() {
        vm.onNameChange("   ")
        vm.onPhoneChange("01012345678")

        vm.requestLocation()

        assertNull(repository.currentShare.value)
    }

    @Test
    fun request_savesTrimmedNameAndSendsEvent() = runTest {
        vm.onNameChange("  홍길동 ")
        vm.onPhoneChange("01012345678")

        vm.requestLocation()

        assertEquals(LocationShareEvent.Allowed, vm.events.first())
        val share = repository.currentShare.value as LocationShareResult.Allowed
        assertEquals("홍길동", share.targetName)
    }

    @Test
    fun saveChecked_savesTargetAfterSuccessfulRequest() = runTest {
        vm.onNameChange("홍길동")
        vm.onPhoneChange("01012345678")
        vm.onSaveForNextChange(true)

        vm.requestLocation()

        assertEquals(LocationShareEvent.Allowed, vm.events.first())
        assertEquals(listOf("홍길동"), targetRepository.stored.map { it.name })
    }

    @Test
    fun saveUnchecked_doesNotSaveTarget() = runTest {
        vm.onNameChange("홍길동")
        vm.onPhoneChange("01012345678")

        vm.requestLocation()

        assertEquals(LocationShareEvent.Allowed, vm.events.first())
        assertTrue(targetRepository.stored.isEmpty())
    }

    @Test
    fun waitingForResponse_showsWaiting_thenHidesAfterResponse() = runTest {
        val slowRepository = FakePickupRepository(responseDelay = 3.seconds)
        val waitingVm = LocationRequestViewModel(SavedStateHandle(), slowRepository, targetRepository)
        waitingVm.onNameChange("홍길동")
        waitingVm.onPhoneChange("01012345678")

        waitingVm.requestLocation()
        assertTrue(waitingVm.uiState.value.isRequesting)

        advanceTimeBy(3.seconds)
        runCurrent()
        assertEquals(LocationShareEvent.Allowed, waitingVm.events.first())
        assertFalse(waitingVm.uiState.value.isRequesting)
    }

    @Test
    fun cancelWhileWaiting_staysWithInputsAndSendsNoEvent() = runTest {
        val slowRepository = FakePickupRepository(responseDelay = 10.seconds)
        val waitingVm = LocationRequestViewModel(SavedStateHandle(), slowRepository, targetRepository)
        val events = mutableListOf<LocationShareEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { waitingVm.events.collect { events += it } }
        waitingVm.onNameChange("홍길동")
        waitingVm.onPhoneChange("01012345678")
        waitingVm.onSaveForNextChange(true)

        waitingVm.requestLocation()
        waitingVm.cancelRequest()
        advanceUntilIdle()

        assertFalse(waitingVm.uiState.value.isRequesting)
        assertTrue(events.isEmpty())
        assertNull(slowRepository.currentShare.value)
        assertTrue(targetRepository.stored.isEmpty()) // 취소했으니 저장도 안 됨
        assertEquals("홍길동", waitingVm.uiState.value.name)
    }
}
