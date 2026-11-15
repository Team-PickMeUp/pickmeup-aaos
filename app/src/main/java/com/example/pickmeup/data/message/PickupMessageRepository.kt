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
 * 시연용 가짜 메시지: 구독을 시작한 때(= 내비 안내 시작 후 픽업 진행 중 화면 진입)부터
 * [schedule] 의 시각에 메시지를 한 번씩만 보냅니다. 반복하지 않습니다.
 *
 * 기본 시나리오: 5초 가는 중 → 10초 거의 도착 → 15초 대기 중 (20초에 도착: NaviRepository.arrivals)
 */
@Singleton
class FakePickupMessageRepository internal constructor(
    /** 구독 시작 기준 시각 → 메시지 (시각 순) */
    private val schedule: List<Pair<Duration, PickupMessage>>,
) : PickupMessageRepository {

    @Inject
    constructor() : this(DEMO_SCHEDULE)

    override val messages: Flow<PickupMessage> = flow {
        var elapsed = Duration.ZERO
        schedule.forEach { (at, message) ->
            delay(at - elapsed)
            elapsed = at
            emit(message)
        }
    }

    companion object {
        val DEMO_SCHEDULE = listOf(
            5.seconds to PickupMessage.GoingToPlace,
            10.seconds to PickupMessage.AlmostArrived,
            15.seconds to PickupMessage.Waiting,
        )
    }
}
