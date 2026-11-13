package com.example.pickmeup.model

/** 픽업 대상자가 운전자에게 보내는 상황 메시지 (Figma 6-1-4-1~3) */
enum class PickupMessage {
    /** 픽업 위치로 가는 중 */
    GoingToPlace,

    /** 거의 다 도착 */
    AlmostArrived,

    /** 픽업 장소에서 대기 중 */
    Waiting,
}
