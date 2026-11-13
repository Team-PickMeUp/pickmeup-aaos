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

@Serializable
data object LocationRequestDestination

/** 위치 공유 결과 — 경유지 / 목적지 선택 */
@Serializable
data object ShareResultDestination

@Serializable
data object PickupInProgressDestination

@Serializable
data object AppGuideDestination

@Serializable
data object SettingsDestination
