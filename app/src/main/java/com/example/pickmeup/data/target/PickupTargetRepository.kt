package com.example.pickmeup.data.target

import android.content.Context
import androidx.core.content.edit
import com.example.pickmeup.model.PickupTarget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** 저장된 픽업 대상자 프로필 저장소 */
interface PickupTargetRepository {

    /** 저장된 대상자 목록 (최근 저장 순) */
    val targets: StateFlow<List<PickupTarget>>

    /** 대상자 저장. 같은 전화번호가 있으면 이름을 덮어쓰고 맨 앞으로 */
    fun save(name: String, phoneDigits: String, nowMillis: Long = System.currentTimeMillis())
}

/**
 * 픽업 대상자를 기기(SharedPreferences, JSON)에 저장합니다.
 * 제3자(픽업 대상자)의 개인정보라 백업 대상에서 제외되어 있습니다
 * (res/xml/backup_rules.xml, data_extraction_rules.xml).
 */
@Singleton
class LocalPickupTargetRepository @Inject constructor(
    @ApplicationContext context: Context,
) : PickupTargetRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _targets = MutableStateFlow(load())
    override val targets: StateFlow<List<PickupTarget>> = _targets.asStateFlow()

    override fun save(name: String, phoneDigits: String, nowMillis: Long) {
        _targets.update { current ->
            listOf(PickupTarget(name, phoneDigits, nowMillis)) +
                current.filterNot { it.phoneDigits == phoneDigits }
        }
        persist(_targets.value)
    }

    private fun load(): List<PickupTarget> {
        val raw = prefs.getString(KEY_TARGETS, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<StoredTarget>>(raw)
                .map { PickupTarget(it.name, it.phoneDigits, it.savedAt) }
                .sortedByDescending { it.savedAt }
        } catch (e: SerializationException) {
            emptyList() // 저장 형식이 깨졌으면 비운 상태로 시작
        }
    }

    private fun persist(targets: List<PickupTarget>) {
        val stored = targets.map { StoredTarget(it.name, it.phoneDigits, it.savedAt) }
        prefs.edit { putString(KEY_TARGETS, json.encodeToString(stored)) }
    }

    /** 저장 형식 (모델과 분리해서 저장 형식이 바뀌어도 모델은 그대로 유지) */
    @Serializable
    private data class StoredTarget(val name: String, val phoneDigits: String, val savedAt: Long)

    private companion object {
        const val PREFS_NAME = "pickup_targets"
        const val KEY_TARGETS = "targets"
    }
}
