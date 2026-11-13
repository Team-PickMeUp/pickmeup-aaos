package com.example.pickmeup.navigation

import com.example.pickmeup.ui.terms.TermsType
import kotlinx.serialization.Serializable

// Navigation Compose 화면 경로 (type-safe route)
// 이름: XxxDestination — 화면 Composable(XxxRoute)과 구분

/** 최초 실행 약관 동의 */
@Serializable
data object TermsAgreementDestination

/** 약관 상세 (동의 화면, 설정 화면에서 열림) */
@Serializable
data class TermsDetailDestination(val type: TermsType)

@Serializable
data object MainDestination

/**
 * 위치 요청. 저장된 대상자를 골라서 들어오면 이름·전화번호가 채워진 상태로 시작
 * (인자 이름이 LocationRequestViewModel 의 SavedStateHandle 키와 같아서 자동으로 들어감)
 */
@Serializable
data class LocationRequestDestination(
    val name: String = "",
    val phoneDigits: String = "",
)

/** 픽업 불러오기 — 저장된 대상자 선택 */
@Serializable
data object PickupTargetSelectDestination

/** 위치 공유 결과 — 경유지 / 목적지 선택 */
@Serializable
data object ShareResultDestination

@Serializable
data object PickupInProgressDestination

@Serializable
data object AppGuideDestination

@Serializable
data object SettingsDestination
