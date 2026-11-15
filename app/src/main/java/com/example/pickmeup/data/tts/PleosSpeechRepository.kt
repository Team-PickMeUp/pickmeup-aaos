package com.example.pickmeup.data.tts

import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import ai.pleos.playground.tts.listener.OnInitializeListener
import ai.pleos.playground.tts.listener.OnServerConnectionListener
import android.content.Context
import android.util.Log
import com.example.pickmeup.BuildConfig
import com.example.pickmeup.data.runSuspendCatching
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

/**
 * Pleos TextToSpeech SDK 로 차량 음성 서비스(Speech Service)에 발화를 요청합니다.
 *
 * Pleos 가이드 순서: `TextToSpeech(context, mode)` → `initialize(listener)` → `addEventListener()`
 * → `setLocale()` → (HYBRID/SERVER 면) `registerApp()` 후 onConnected → `speak()`.
 * 앱 전체에서 하나만 쓰는 싱글턴이라 앱이 살아 있는 동안 연결을 유지합니다 (release 는 하지 않음).
 *
 * 모드: local.properties 에 TTS Client ID/Secret 이 있으면 HYBRID, 없으면 등록이 필요 없는 ON_DEVICE.
 */
@Singleton
class PleosSpeechRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechRepository {

    private val clientId = BuildConfig.TTS_CLIENT_ID
    private val clientSecret = BuildConfig.TTS_CLIENT_SECRET
    private val useServer = clientId.isNotBlank() && clientSecret.isNotBlank()

    private val tts by lazy { TextToSpeech(context, if (useServer) Mode.HYBRID else Mode.ON_DEVICE) }

    /** SDK 콜백은 다른 스레드에서 올 수 있어 [lock] 으로 보호 */
    private val lock = Any()

    /** 준비 결과. 실패(예외로 완료)하면 다음 speak 때 다시 연결 */
    private var connection: CompletableDeferred<Unit>? = null

    /** 지금 읽는 중인 발화. 완료·중지·오류 이벤트가 오면 완료 */
    private var utterance: CompletableDeferred<Unit>? = null

    private val eventListener = object : EventListener {
        override fun onReady() = Unit
        override fun onUpdatedRms(rms: Double) = Unit

        override fun onStart() {
            Log.d(TAG, "onStart")
        }

        override fun onDone() {
            Log.d(TAG, "onDone")
            finishUtterance()
        }

        override fun onStop() {
            Log.d(TAG, "onStop")
            finishUtterance()
        }

        override fun onError(errorType: ErrorType) {
            Log.w(TAG, "onError $errorType")
            finishUtterance()
        }
    }

    override fun connect() {
        startConnection()
    }

    /** 진행 중이거나 성공한 연결은 재사용, 실패한 연결은 새로 시작 */
    private fun startConnection(): CompletableDeferred<Unit> {
        val deferred = synchronized(lock) {
            // 예외로 완료된 Deferred 는 isCancelled == true
            connection?.takeUnless { it.isCancelled }?.let { return it }
            CompletableDeferred<Unit>().also { connection = it }
        }
        Log.d(TAG, "initialize mode=${if (useServer) "HYBRID" else "ON_DEVICE"}")
        tts.initialize(object : OnInitializeListener {
            override fun onSuccess() {
                tts.addEventListener(eventListener)
                tts.setLocale(Locale.KOREA)
                if (useServer) registerApp(deferred) else markReady(deferred)
            }

            override fun onFailure(errorType: ErrorType) = markFailed(deferred, "initialize", errorType)
        })
        return deferred
    }

    /** HYBRID 모드: 서버 TTS 를 쓰려면 앱 등록 후 onConnected 를 기다려야 함 */
    private fun registerApp(deferred: CompletableDeferred<Unit>) {
        tts.registerApp(clientId, clientSecret, object : OnServerConnectionListener {
            override fun onConnected() = markReady(deferred)

            override fun onFailed(errorType: ErrorType) = markFailed(deferred, "registerApp", errorType)
        })
    }

    override suspend fun speak(textRes: Int) {
        val text = context.getString(textRes)
        val connection = startConnection()
        // 준비 전이면 잠깐 기다림. 실패하거나 너무 오래 걸리면 음성 없이 넘어감 (화면 알림은 그대로)
        val ready = withTimeoutOrNull(READY_TIMEOUT) { runSuspendCatching { connection.await() }.isSuccess } ?: false
        if (!ready) return

        val done = CompletableDeferred<Unit>()
        synchronized(lock) {
            // 이전 발화를 기다리던 쪽이 있으면 놓아줌 (새 발화가 이어서 나감)
            utterance?.complete(Unit)
            utterance = done
        }
        tts.speak(text, true)
        // 완료 이벤트가 오지 않는 경우(실기기에서 가끔 누락)에도 영원히 기다리지 않음
        withTimeoutOrNull(SPEAK_TIMEOUT) { done.await() }
        synchronized(lock) { if (utterance === done) utterance = null }
    }

    override fun stop() {
        val ready = synchronized(lock) { connection?.isCompleted == true && connection?.isCancelled == false }
        if (ready) tts.stop()
        finishUtterance()
    }

    private fun finishUtterance() {
        synchronized(lock) { utterance?.complete(Unit) }
    }

    private fun markReady(deferred: CompletableDeferred<Unit>) {
        Log.d(TAG, "ready")
        deferred.complete(Unit)
    }

    private fun markFailed(deferred: CompletableDeferred<Unit>, step: String, errorType: ErrorType) {
        Log.w(TAG, "$step failed: $errorType")
        deferred.completeExceptionally(IllegalStateException("TTS $step failed: $errorType"))
    }

    private companion object {
        const val TAG = "PickMeUpTts"

        /** speak 시 연결 준비를 기다리는 최대 시간 */
        val READY_TIMEOUT = 3.seconds

        /** 발화 완료 이벤트를 기다리는 최대 시간 (메시지 하나는 실기기에서 약 5초) */
        val SPEAK_TIMEOUT = 10.seconds
    }
}
