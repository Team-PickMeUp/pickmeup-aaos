package com.example.pickmeup

import android.app.Application
import com.example.pickmeup.data.navi.NaviRepository
import com.example.pickmeup.data.tts.SpeechRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PickMeUpApplication : Application() {

    @Inject
    lateinit var naviRepository: NaviRepository

    @Inject
    lateinit var speechRepository: SpeechRepository

    override fun onCreate() {
        super.onCreate()
        // Pleos 가이드: 앱 시작 시 NaviHelper 를 만들고 initialize() → 첫 요청 전에 연결이 끝나 있도록 미리 시작
        naviRepository.connect()
        // Pleos 가이드: TextToSpeech 도 시작 시 initialize → 첫 알림 전에 발화 준비
        speechRepository.connect()
    }
}
