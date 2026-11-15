package com.example.pickmeup.ui.share

import androidx.lifecycle.SavedStateHandle
import com.example.pickmeup.MainDispatcherRule
import com.example.pickmeup.data.message.FakePickupMessageRepository
import com.example.pickmeup.data.navi.FakeNaviRepository
import com.example.pickmeup.data.pickup.FakePickupRepository
import com.example.pickmeup.data.tts.FakeSpeechRepository
import com.example.pickmeup.model.LocationShareResult
import com.example.pickmeup.model.PickupMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class PickupInProgressViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakePickupRepository(responseDelay = Duration.ZERO)
    /** 기본 시연 시나리오와 같은 타이밍: 20초에 도착 */
    private val navi = FakeNaviRepository(simulatedArrival = 20.seconds)
    private val speech = FakeSpeechRepository()

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) =
        PickupInProgressViewModel(handle, repository, FakePickupMessageRepository(FakePickupMessageRepository.DEMO_SCHEDULE), navi, speech)

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
    fun demoScenario_eachMessageOnceForTwoSeconds_thenArrives() = runTest {
        repository.requestLocationShare("홍길동", "01012345678")
        val vm = viewModel()

        fun at(seconds: Int): PickupMessage? {
            advanceTimeBy(seconds.seconds - currentTime.milliseconds); runCurrent()
            return vm.message.value
        }

        assertNull(at(4))
        assertEquals(PickupMessage.GoingToPlace, at(5))
        assertEquals(PickupMessage.GoingToPlace, at(6))
        assertNull(at(7)) // 2초 노출 후 올라감
        assertEquals(PickupMessage.AlmostArrived, at(10))
        assertNull(at(12))
        assertEquals(PickupMessage.Waiting, at(15))
        assertNull(at(17))
        assertFalse(vm.arrived.value)

        assertNull(at(20))
        assertTrue(vm.arrived.value) // 20초에 도착 화면

        assertNull(at(60)) // 반복하지 않음
    }

    @Test
    fun complete_endsNaviGuidance() = runTest {
        val share = repository.requestLocationShare("홍길동", "01012345678").getOrThrow() as LocationShareResult.Allowed
        navi.setWaypoint(share)
        val vm = viewModel()

        vm.completePickup()

        assertEquals(1, navi.endGuidanceCount)
        assertNull(navi.placed)
    }

    @Test
    fun cancel_endsNaviGuidance() = runTest {
        val share = repository.requestLocationShare("홍길동", "01012345678").getOrThrow() as LocationShareResult.Allowed
        navi.setDestination(share)
        val vm = viewModel()

        vm.cancelPickup()

        assertEquals(1, navi.endGuidanceCount)
    }

    @Test
    fun afterFinish_notificationIsHidden() = runTest {
        repository.requestLocationShare("홍길동", "01012345678")
        val vm = viewModel()
        advanceTimeBy(5.seconds); runCurrent()
        assertNotNull(vm.message.value)

        vm.completePickup()

        assertNull(vm.message.value)
    }

    @Test
    fun eachNotification_isSpokenOnce_withCardText() = runTest {
        repository.requestLocationShare("홍길동", "01012345678")
        viewModel()

        advanceTimeBy(5.seconds); runCurrent()
        assertEquals(listOf(PickupMessage.GoingToPlace.textRes), speech.spoken)

        advanceTimeBy(60.seconds); runCurrent()
        assertEquals(PickupMessage.entries.map { it.textRes }, speech.spoken)
    }

    @Test
    fun notification_staysUntilSpeechEnds() = runTest {
        // 실기기 발화 시간과 비슷하게 4.6초
        val speaking = FakeSpeechRepository(speechDuration = 4600.milliseconds)
        repository.requestLocationShare("홍길동", "01012345678")
        val vm = PickupInProgressViewModel(
            SavedStateHandle(), repository,
            FakePickupMessageRepository(FakePickupMessageRepository.DEMO_SCHEDULE), navi, speaking,
        )

        advanceTimeBy(5.seconds); runCurrent()
        assertEquals(PickupMessage.GoingToPlace, vm.message.value)

        advanceTimeBy(4500.milliseconds); runCurrent() // 9.5초: 아직 읽는 중
        assertEquals(PickupMessage.GoingToPlace, vm.message.value)

        advanceTimeBy(200.milliseconds); runCurrent() // 9.7초: 발화 끝 → 올라감
        assertNull(vm.message.value)

        // 다음 메시지는 10초에 도착했지만, 앞 알림이 올라간 9.6초 + 간격 2초 = 11.6초까지 대기
        advanceTimeBy(1800.milliseconds); runCurrent() // 11.5초
        assertNull(vm.message.value)
        advanceTimeBy(100.milliseconds); runCurrent() // 11.6초
        assertEquals(PickupMessage.AlmostArrived, vm.message.value)

        // 거의 도착: 11.6 ~ 16.2초 → 간격 → 대기 중 18.2초
        advanceTimeBy(6500.milliseconds); runCurrent() // 18.1초
        assertNull(vm.message.value)
        advanceTimeBy(100.milliseconds); runCurrent() // 18.2초
        assertEquals(PickupMessage.Waiting, vm.message.value)
    }

    @Test
    fun finishingPickup_stopsSpeech() = runTest {
        repository.requestLocationShare("홍길동", "01012345678")
        val vm = viewModel()

        vm.cancelPickup()

        assertEquals(1, speech.stopCount)
    }
}
