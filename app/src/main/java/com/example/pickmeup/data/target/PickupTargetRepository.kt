package com.example.pickmeup.data.target

import android.content.Context
import androidx.core.content.edit
import com.example.pickmeup.data.runSuspendCatching
import com.example.pickmeup.model.PickupTarget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 저장된 픽업 대상자 프로필 저장소.
 *
 * 서버로 옮겨도 화면·ViewModel 을 고치지 않도록 서버 통신 기준으로 설계했습니다.
 * - 모든 작업은 suspend + Result (기다릴 수 있고, 실패할 수 있음)
 * - [targets] 는 마지막으로 불러온 목록. 화면 진입 시 [refresh] 로 갱신
 * - 서버 연동 시: `RemotePickupTargetRepository` 를 만들고 di/RepositoryModule 에서 바꿔 연결
 */
interface PickupTargetRepository {

    /** 저장된 대상자 목록 (최근 저장 순) */
    val targets: StateFlow<List<PickupTarget>>

    /** 저장소에서 목록을 다시 불러와 [targets] 갱신 */
    suspend fun refresh(): Result<Unit>

    /** 대상자 저장. 같은 전화번호가 있으면 이름을 덮어쓰고 맨 앞으로 */
    suspend fun save(name: String, phoneDigits: String): Result<PickupTarget>
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
    /** 저장이 동시에 일어나도 목록이 꼬이지 않도록 */
    private val mutex = Mutex()

    private val _targets = MutableStateFlow<List<PickupTarget>>(emptyList())
    override val targets: StateFlow<List<PickupTarget>> = _targets.asStateFlow()

    override suspend fun refresh(): Result<Unit> = runSuspendCatching {
        mutex.withLock { _targets.value = load() }
    }

    override suspend fun save(name: String, phoneDigits: String): Result<PickupTarget> = runSuspendCatching {
        mutex.withLock {
            val current = load()
            val saved = PickupTarget(
                // 같은 번호면 기존 ID 유지
                id = current.find { it.phoneDigits == phoneDigits }?.id ?: UUID.randomUUID().toString(),
                name = name,
                phoneDigits = phoneDigits,
                savedAt = System.currentTimeMillis(),
            )
            val updated = listOf(saved) + current.filterNot { it.phoneDigits == phoneDigits }
            persist(updated)
            _targets.value = updated
            saved
        }
    }

    private suspend fun load(): List<PickupTarget> = withContext(Dispatchers.IO) {
        val raw = prefs.getString(KEY_TARGETS, null) ?: return@withContext emptyList()
        try {
            json.decodeFromString<List<StoredTarget>>(raw)
                .map { PickupTarget(it.id, it.name, it.phoneDigits, it.savedAt) }
                .sortedByDescending { it.savedAt }
        } catch (e: SerializationException) {
            emptyList() // 저장 형식이 깨졌으면 비운 상태로 시작
        }
    }

    private suspend fun persist(targets: List<PickupTarget>) = withContext(Dispatchers.IO) {
        val stored = targets.map { StoredTarget(it.id, it.name, it.phoneDigits, it.savedAt) }
        prefs.edit(commit = true) { putString(KEY_TARGETS, json.encodeToString(stored)) }
    }

    /** 저장 형식 (모델과 분리해서 저장 형식이 바뀌어도 모델은 그대로 유지) */
    @Serializable
    private data class StoredTarget(
        val id: String,
        val name: String,
        val phoneDigits: String,
        val savedAt: Long,
    )

    private companion object {
        const val PREFS_NAME = "pickup_targets"
        const val KEY_TARGETS = "targets"
    }
}
