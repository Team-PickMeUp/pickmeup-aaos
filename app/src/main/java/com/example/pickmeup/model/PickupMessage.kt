package com.example.pickmeup.model

import androidx.annotation.StringRes
import com.example.pickmeup.R

/**
 * 픽업 대상자가 운전자에게 보내는 상황 메시지 (Figma 6-1-4-1~3).
 * [textRes] 는 알림 카드 문구이자 TTS 로 읽는 문구입니다.
 */
enum class PickupMessage(@StringRes val textRes: Int) {
    /** 픽업 위치로 가는 중 */
    GoingToPlace(R.string.pickup_message_going),

    /** 거의 다 도착 */
    AlmostArrived(R.string.pickup_message_almost),

    /** 픽업 장소에서 대기 중 */
    Waiting(R.string.pickup_message_waiting),
}
