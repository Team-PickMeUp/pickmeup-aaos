package com.example.pickmeup.data.message

import com.example.pickmeup.model.PickupMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 픽업 대상자 상황 메시지 수신.
 * 서버 연동 시: WebSocket/Push 로 받은 메시지를 [messages] 로 흘려보내는 구현을 만들고
 * di/RepositoryModule 에서 바꿔 연결 (Figma 개발 명세 9-6)
 */
interface PickupMessageRepository {

    /** 진행 중인 픽업에 대해 대상자가 보낸 메시지 (받을 때마다 하나씩) */
    val messages: Flow<PickupMessage>
}

/**
 * 화면 확인용 가짜 메시지: [interval] 마다 가는 중 → 거의 도착 → 대기 중을 계속 반복합니다.
 */
@Singleton
class FakePickupMessageRepository internal constructor(
    private val interval: Duration,
) : PickupMessageRepository {

    @Inject
    constructor() : this(interval = 2.seconds)

    override val messages: Flow<PickupMessage> = flow {
        while (true) {
            PickupMessage.entries.forEach { message ->
                emit(message)
                delay(interval)
            }
        }
    }
}
