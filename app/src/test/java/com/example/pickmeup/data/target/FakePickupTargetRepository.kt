package com.example.pickmeup.data.target

import com.example.pickmeup.model.PickupTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 테스트용 픽업 대상자 저장소.
 * 서버처럼 [refresh] 를 불러야 [targets] 가 채워집니다.
 */
class FakePickupTargetRepository(initial: List<PickupTarget> = emptyList()) : PickupTargetRepository {

    /** 저장소에 실제로 들어 있는 값 (refresh 전에는 화면에 안 보임) */
    var stored: List<PickupTarget> = initial
        private set
    var refreshCount = 0
        private set

    private val state = MutableStateFlow<List<PickupTarget>>(emptyList())
    override val targets: StateFlow<List<PickupTarget>> = state

    override suspend fun refresh(): Result<Unit> {
        refreshCount++
        state.value = stored
        return Result.success(Unit)
    }

    override suspend fun save(name: String, phoneDigits: String): Result<PickupTarget> {
        val saved = PickupTarget(id = phoneDigits, name = name, phoneDigits = phoneDigits, savedAt = stored.size.toLong())
        stored = listOf(saved) + stored.filterNot { it.phoneDigits == phoneDigits }
        state.value = stored
        return Result.success(saved)
    }

    override suspend fun update(id: String, name: String, phoneDigits: String): Result<PickupTarget> {
        val original = stored.find { it.id == id }
            ?: return Result.failure(NoSuchElementException(id))
        val updated = original.copy(name = name, phoneDigits = phoneDigits)
        stored = stored
            .filterNot { it.id != id && it.phoneDigits == phoneDigits }
            .map { if (it.id == id) updated else it }
        state.value = stored
        return Result.success(updated)
    }

    override suspend fun delete(id: String): Result<Unit> {
        stored = stored.filterNot { it.id == id }
        state.value = stored
        return Result.success(Unit)
    }
}
