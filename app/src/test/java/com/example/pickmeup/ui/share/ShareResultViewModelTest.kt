package com.example.pickmeup.ui.share

import com.example.pickmeup.MainDispatcherRule
import com.example.pickmeup.data.navi.FakeNaviRepository
import com.example.pickmeup.data.navi.NaviPlacement
import com.example.pickmeup.data.navi.NaviRepository
import com.example.pickmeup.data.navi.NaviWaypointFullException
import com.example.pickmeup.data.pickup.FakePickupRepository
import com.example.pickmeup.model.LocationShareResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class ShareResultViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val location = LocationShareResult.Allowed("홍길동", "서울시 강남구", 37.48, 127.03)

    private fun viewModel(navi: NaviRepository) =
        ShareResultViewModel(FakePickupRepository(Duration.ZERO), navi)

    private fun TestScope.collectStarted(vm: ShareResultViewModel): MutableList<Unit> {
        val started = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.pickupStarted.collect { started += it } }
        return started
    }

    @Test
    fun waypoint_sendsToNavi_thenStartsPickup() = runTest {
        val navi = FakeNaviRepository()
        val vm = viewModel(navi)
        val started = collectStarted(vm)

        vm.setAsWaypoint(location)

        assertEquals(location to NaviPlacement.Waypoint, navi.placed)
        assertEquals(1, started.size)
        assertFalse(vm.isProcessing.value)
    }

    @Test
    fun destination_sendsToNavi_thenStartsPickup() = runTest {
        val navi = FakeNaviRepository()
        val vm = viewModel(navi)
        val started = collectStarted(vm)

        vm.setAsDestination(location)

        assertEquals(NaviPlacement.Destination, navi.placed?.second)
        assertEquals(1, started.size)
    }

    @Test
    fun naviFailure_staysOnScreen_andAllowsRetry() = runTest {
        val vm = viewModel(object : NaviRepository by FakeNaviRepository() {
            override suspend fun setWaypoint(location: LocationShareResult.Allowed) =
                Result.failure<NaviPlacement>(NaviWaypointFullException())
        })
        val started = collectStarted(vm)

        vm.setAsWaypoint(location)

        assertTrue(started.isEmpty())
        assertFalse(vm.isProcessing.value)
    }

    @Test
    fun whileNaviProcessing_secondTapIsIgnored() = runTest {
        val gate = CompletableDeferred<Unit>()
        var calls = 0
        val vm = viewModel(object : NaviRepository by FakeNaviRepository() {
            override suspend fun setDestination(location: LocationShareResult.Allowed): Result<Unit> {
                calls++
                gate.await()
                return Result.success(Unit)
            }
        })

        vm.setAsDestination(location)
        assertTrue(vm.isProcessing.value)
        vm.setAsDestination(location)
        vm.setAsWaypoint(location)
        gate.complete(Unit)

        assertEquals(1, calls)
        assertFalse(vm.isProcessing.value)
    }
}
