package com.example.pickmeup.data.tts

import androidx.annotation.StringRes

/**
 * 차량 스피커로 문구를 읽어주는 저장소 (Pleos TextToSpeech SDK).
 * 픽업 대상자 상황 메시지 음성 안내에 씁니다 (Figma 9-6).
 */
interface SpeechRepository {

    /** TTS 서비스 연결 시작 (앱 시작 시 한 번). 결과를 기다리지 않습니다. */
    fun connect()

    /**
     * 문구를 읽고, **읽기가 끝날 때까지** 기다립니다 (onDone / onStop / onError).
     * 화면 알림을 발화 시간에 맞춰 보여주려고 씁니다.
     *
     * TTS 를 쓸 수 없으면(연결 실패 등) 바로 돌아오고, 완료 이벤트가 오지 않아도 정해진 시간 뒤에는 돌아옵니다.
     * 실패해도 화면 흐름을 막지 않도록 예외를 던지지 않습니다. 코루틴이 취소되면 기다리기만 멈춥니다.
     */
    suspend fun speak(@StringRes textRes: Int)

    /** 읽는 중인 음성을 멈춥니다. */
    fun stop()
}
