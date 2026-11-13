package com.example.pickmeup.model

/** 저장된 픽업 대상자 프로필 */
data class PickupTarget(
    val name: String,
    /** 숫자만 (예: 01012345678). 같은 번호는 한 명으로 취급 */
    val phoneDigits: String,
    /** 마지막 저장 시각. 최근 순 정렬에 사용 */
    val savedAt: Long,
)
