package com.example.pickmeup.model

/** 픽업 대상자의 위치 공유 응답 결과 */
sealed interface LocationShareResult {

    /** 허용: 공유받은 위치 */
    data class Allowed(
        val targetName: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
    ) : LocationShareResult

    /** 미허용 */
    data class Denied(val targetName: String) : LocationShareResult
}
