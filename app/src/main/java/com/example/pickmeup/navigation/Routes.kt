package com.example.pickmeup.navigation

import com.example.pickmeup.ui.terms.TermsType
import kotlinx.serialization.Serializable

// Navigation Compose 화면 경로 (type-safe route)

/** 최초 실행 약관 동의 */
@Serializable
data object TermsAgreementRoute

/** 약관 상세 (동의 화면, 설정 화면에서 열림) */
@Serializable
data class TermsDetailRoute(val type: TermsType)

@Serializable
data object MainRoute

@Serializable
data object LocationRequestRoute

/** 위치 공유 결과 — 경유지 / 목적지 선택 */
@Serializable
data object ShareResultRoute

@Serializable
data object PickupInProgressRoute

@Serializable
data object AppGuideRoute

@Serializable
data object SettingsRoute
