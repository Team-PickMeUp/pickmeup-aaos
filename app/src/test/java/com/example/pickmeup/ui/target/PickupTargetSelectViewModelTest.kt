package com.example.pickmeup.ui.target

import com.example.pickmeup.MainDispatcherRule
import com.example.pickmeup.data.target.PickupTargetRepository
import com.example.pickmeup.model.PickupTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PickupTargetSelectViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeTargetRepository(initial: List<PickupTarget>) : PickupTargetRepository {
        private val state = MutableStateFlow(initial)
        override val targets: StateFlow<List<PickupTarget>> = state
        override fun save(name: String, phoneDigits: String, nowMillis: Long) {
            state.update { listOf(PickupTarget(name, phoneDigits, nowMillis)) + it }
        }
    }

    private fun target(name: String, savedAt: Long) = PickupTarget(name, "0101234567$savedAt", savedAt)

    @Test
    fun noSavedTargets_showsNothing() {
        val vm = PickupTargetSelectViewModel(FakeTargetRepository(emptyList()))

        assertTrue(vm.recentTargets.value.isEmpty())
    }

    @Test
    fun moreThanThreeTargets_showsOnlyRecentThree() {
        val saved = listOf(target("가", 5), target("나", 4), target("다", 3), target("라", 2))
        val vm = PickupTargetSelectViewModel(FakeTargetRepository(saved))

        assertEquals(listOf("가", "나", "다"), vm.recentTargets.value.map { it.name })
    }
}
