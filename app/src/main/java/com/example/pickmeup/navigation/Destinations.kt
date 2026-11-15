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

/**
 * 위치 요청 — "새 픽업 대상자 입력".
 * 이름·전화번호를 넘기면 채워진 상태로 시작
 * (인자 이름이 LocationRequestViewModel 의 SavedStateHandle 키와 같아서 자동으로 들어감).
 * 지금은 픽업 불러오기의 "새 픽업 대상자 입력"에서 빈 값으로만 열림 (전체 프로필 보기는 팝업으로 바로 요청)
 */
@Serializable
data class LocationRequestDestination(
    val name: String = "",
    val phoneDigits: String = "",
)

/** 픽업 불러오기 — 저장된 대상자 선택. 약관 동의 후 앱 첫 화면 */
@Serializable
data object PickupTargetSelectDestination

/** 저장된 픽업 대상자 전체 목록 · 삭제 */
@Serializable
data object PickupTargetManageDestination

/** 위치 공유 결과 — 경유지 / 목적지 선택 */
@Serializable
data object ShareResultDestination

@Serializable
data object PickupInProgressDestination

@Serializable
data object AppGuideDestination

@Serializable
data object SettingsDestination
