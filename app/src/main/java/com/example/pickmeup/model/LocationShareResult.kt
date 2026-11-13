package com.example.pickmeup.ui.share

import androidx.compose.runtime.saveable.Saver

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

    companion object {
        /** rememberSaveable 용 (화면 회전·프로세스 재생성 시 유지) */
        val Saver: Saver<LocationShareResult?, List<Any>> = Saver(
            save = { result ->
                when (result) {
                    is Allowed -> listOf("allowed", result.targetName, result.address, result.latitude, result.longitude)
                    is Denied -> listOf("denied", result.targetName)
                    null -> listOf("none")
                }
            },
            restore = { list ->
                when (list[0]) {
                    "allowed" -> Allowed(list[1] as String, list[2] as String, list[3] as Double, list[4] as Double)
                    "denied" -> Denied(list[1] as String)
                    else -> null
                }
            },
        )
    }
}
