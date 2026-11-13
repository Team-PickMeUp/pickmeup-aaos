package com.example.pickmeup

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.pickmeup.data.TermsConsentRepository
import com.example.pickmeup.ui.guide.AppGuideScreen
import com.example.pickmeup.ui.location.LocationRequestScreen
import com.example.pickmeup.ui.main.MainScreen
import com.example.pickmeup.ui.settings.SettingsScreen
import com.example.pickmeup.ui.share.LocationShareResult
import com.example.pickmeup.ui.share.LocationShareResultScreen
import com.example.pickmeup.ui.share.PickupInProgressScreen
import com.example.pickmeup.ui.terms.TermsAgreementScreen
import com.example.pickmeup.ui.terms.TermsDetailScreen
import com.example.pickmeup.ui.terms.TermsType
import com.example.pickmeup.ui.theme.PickMeUpTheme

class MainActivity : ComponentActivity() {

    private val consentRepository by lazy { TermsConsentRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PickMeUpTheme(dynamicColor = false) {
                var agreed by rememberSaveable {
                    mutableStateOf(consentRepository.hasAgreedToCurrentTerms())
                }
                if (agreed) {
                    AppFlow()
                } else {
                    TermsFlow(
                        onAgreeAndStart = {
                            consentRepository.saveAgreement()
                            agreed = true
                        },
                        // 동의하지 않으면 앱을 시작하지 않고 종료 → 런처로 돌아감
                        onDisagree = { finish() },
                    )
                }
            }
        }
    }
}

@Composable
private fun TermsFlow(
    onAgreeAndStart: () -> Unit,
    onDisagree: () -> Unit,
) {
    var detail by rememberSaveable { mutableStateOf<TermsType?>(null) }
    val current = detail
    if (current != null) {
        BackHandler { detail = null }
        TermsDetailScreen(type = current, onBack = { detail = null })
    } else {
        TermsAgreementScreen(
            onAgreeAndStart = onAgreeAndStart,
            onDisagree = onDisagree,
            onViewDetail = { detail = it },
        )
    }
}

private enum class Screen { Main, LocationRequest, ShareResult, PickupInProgress, AppGuide, Settings, LocationTerms, PrivacyTerms }

/**
 * 약관 동의 이후 화면 흐름.
 * 설정 화면은 여러 화면에서 열 수 있어서 간단한 뒤로 가기 스택으로 관리합니다.
 * 이미 스택에 있는 화면으로 이동하면 그 화면까지 되돌아갑니다(설정 ↔ 가이드 무한 반복 방지).
 */
@Composable
private fun AppFlow() {
    val context = LocalContext.current
    val comingSoon = stringResource(R.string.coming_soon)
    val showComingSoon: () -> Unit = {
        Toast.makeText(context, comingSoon, Toast.LENGTH_SHORT).show()
    }

    var backStack by rememberSaveable { mutableStateOf(listOf(Screen.Main)) }
    var shareResult by rememberSaveable(stateSaver = LocationShareResult.Saver) {
        mutableStateOf<LocationShareResult?>(null)
    }
    val navigate: (Screen) -> Unit = { target ->
        val existing = backStack.indexOf(target)
        backStack = if (existing >= 0) backStack.take(existing + 1) else backStack + target
    }
    val goBack: () -> Unit = { backStack = backStack.dropLast(1) }
    // 픽업 종료(완료/취소): 결과를 지우고 메인으로
    val finishPickup: () -> Unit = {
        shareResult = null
        backStack = listOf(Screen.Main)
    }

    BackHandler(enabled = backStack.size > 1, onBack = goBack)

    // 화면별 입력 상태(예: 위치 요청 이름/전화번호)를 뒤로 돌아왔을 때도 유지
    val stateHolder = rememberSaveableStateHolder()
    val current = backStack.last()
    stateHolder.SaveableStateProvider(current.name) {
        when (current) {
            Screen.Main -> MainScreen(
                onSettingsClick = { navigate(Screen.Settings) },
                onRequestLocationClick = { navigate(Screen.LocationRequest) },
                onAppGuideClick = { navigate(Screen.AppGuide) },
            )

            Screen.LocationRequest -> LocationRequestScreen(
                onBack = goBack,
                onSettingsClick = { navigate(Screen.Settings) },
                onRequestLocation = { name, _ ->
                    // TODO: 서버 연동 — 문자 발송 후 대상자의 허용/미허용 응답을 받아 shareResult 에 넣기.
                    //  응답 대기 화면은 Figma에 없음. 지금은 화면 확인용으로 바로 '허용'(예시 주소) 처리.
                    shareResult = LocationShareResult.Allowed(
                        targetName = name,
                        address = "00시 00구 00로",
                        latitude = 0.0,
                        longitude = 0.0,
                    )
                    navigate(Screen.ShareResult)
                },
            )

            Screen.ShareResult -> {
                val result = shareResult
                if (result == null) {
                    // 결과 없이 들어온 경우(비정상)엔 이전 화면으로
                    LaunchedEffect(Unit) { goBack() }
                } else {
                    LocationShareResultScreen(
                        result = result,
                        onBack = goBack,
                        onSettingsClick = { navigate(Screen.Settings) },
                        // TODO: Pleos Connect SDK 내비게이션 연동 — 경유지 추가 후 이동
                        onSetWaypoint = { navigate(Screen.PickupInProgress) },
                        // TODO: Pleos Connect SDK 내비게이션 연동 — 목적지 설정 후 이동
                        onSetDestination = { navigate(Screen.PickupInProgress) },
                    )
                }
            }

            Screen.PickupInProgress -> {
                val location = shareResult as? LocationShareResult.Allowed
                if (location == null) {
                    LaunchedEffect(Unit) { goBack() }
                } else {
                    PickupInProgressScreen(
                        location = location,
                        onBack = goBack,
                        onSettingsClick = { navigate(Screen.Settings) },
                        // TODO: 취소 확인 팝업 여부(기획), 서버에 취소 알림, 내비 경로에서 제거
                        onCancelPickup = finishPickup,
                        // TODO: 서버에 완료 알림(위치 공유 종료)
                        onCompletePickup = finishPickup,
                    )
                }
            }

            Screen.AppGuide -> AppGuideScreen(
                onBack = goBack,
                onSettingsClick = { navigate(Screen.Settings) },
            )

            Screen.Settings -> SettingsScreen(
                onBack = goBack,
                onAppGuideClick = { navigate(Screen.AppGuide) },
                onPickupTargetClick = showComingSoon, // TODO: 픽업 대상자 지정 화면 (Figma 미정)
                onLocationTermsClick = { navigate(Screen.LocationTerms) },
                onPrivacyPolicyClick = { navigate(Screen.PrivacyTerms) },
            )

            Screen.LocationTerms -> TermsDetailScreen(type = TermsType.Location, onBack = goBack)

            Screen.PrivacyTerms -> TermsDetailScreen(type = TermsType.Privacy, onBack = goBack)
        }
    }
}
