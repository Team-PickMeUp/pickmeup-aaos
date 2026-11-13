package com.example.pickmeup.data.remote.dto

import kotlinx.serialization.Serializable

// ⚠️ 초안: 서버 Swagger 문서가 나오면 필드 이름·타입을 맞춰 수정하세요.
// Spring Boot(Jackson) 기본 설정 기준: 필드는 camelCase, enum 은 이름 그대로 직렬화.

@Serializable
data class CreateLocationRequestBody(
    val targetName: String,
    /** 숫자만 (예: 01012345678) */
    val phoneNumber: String,
)

@Serializable
data class LocationRequestResponse(
    val id: Long,
    val status: LocationRequestStatus,
    val targetName: String,
    /** 아래 위치 정보는 status = ALLOWED 일 때만 있음 */
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
enum class LocationRequestStatus {
    /** 대상자 응답 대기 중 */
    PENDING,
    ALLOWED,
    DENIED,
    COMPLETED,
    CANCELED,
}
