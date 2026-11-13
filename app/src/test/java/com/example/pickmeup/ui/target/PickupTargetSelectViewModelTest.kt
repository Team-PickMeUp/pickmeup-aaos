package com.example.pickmeup.ui.target

import com.example.pickmeup.MainDispatcherRule
import com.example.pickmeup.data.target.FakePickupTargetRepository
import com.example.pickmeup.model.PickupTarget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PickupTargetSelectViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun target(name: String, savedAt: Long) =
        PickupTarget(id = name, name = name, phoneDigits = "0101234567$savedAt", savedAt = savedAt)

    /** 화면이 구독 중인 상태를 흉내 (WhileSubscribed 라 구독자가 있어야 값이 갱신됨) */
    private fun TestScope.subscribe(vm: PickupTargetSelectViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.recentTargets.collect {} }
    }

    @Test
    fun entering_refreshesAndShowsTargets() = runTest {
        val repository = FakePickupTargetRepository(listOf(target("가", 1)))

        val vm = PickupTargetSelectViewModel(repository)
        subscribe(vm)

        assertEquals(1, repository.refreshCount)
        assertEquals(listOf("가"), vm.recentTargets.value.map { it.name })
    }

    @Test
    fun noSavedTargets_showsNothing() = runTest {
        val vm = PickupTargetSelectViewModel(FakePickupTargetRepository(emptyList()))
        subscribe(vm)

        assertTrue(vm.recentTargets.value.isEmpty())
    }

    @Test
    fun moreThanThreeTargets_showsOnlyRecentThree() = runTest {
        val saved = listOf(target("가", 5), target("나", 4), target("다", 3), target("라", 2))
        val vm = PickupTargetSelectViewModel(FakePickupTargetRepository(saved))
        subscribe(vm)

        assertEquals(listOf("가", "나", "다"), vm.recentTargets.value.map { it.name })
    }
}
