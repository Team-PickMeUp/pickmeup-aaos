package com.example.pickmeup.data.tts

import kotlinx.coroutines.delay
import kotlin.time.Duration

/**
 * 테스트용: 읽으라고 한 문구(문자열 리소스 ID)와 멈춤 횟수를 기록.
 * [speechDuration] 동안 발화하는 것처럼 기다립니다 (0 이면 TTS 를 못 쓰는 경우처럼 바로 돌아옴).
 */
class FakeSpeechRepository(private val speechDuration: Duration = Duration.ZERO) : SpeechRepository {

    val spoken = mutableListOf<Int>()
    var stopCount = 0
        private set

    override fun connect() = Unit

    override suspend fun speak(textRes: Int) {
        spoken += textRes
        delay(speechDuration)
    }

    override fun stop() {
        stopCount++
    }
}
