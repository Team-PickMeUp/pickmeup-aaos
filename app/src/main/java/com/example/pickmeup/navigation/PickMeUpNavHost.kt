package com.example.pickmeup.navigation

import android.widget.Toast
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.pickmeup.R
import com.example.pickmeup.ui.guide.AppGuideScreen
import com.example.pickmeup.ui.location.LocationRequestRoute
import com.example.pickmeup.ui.main.MainScreen
import com.example.pickmeup.ui.settings.SettingsScreen
import com.example.pickmeup.ui.share.LocationShareResultRoute
import com.example.pickmeup.ui.share.PickupInProgressRoute
import com.example.pickmeup.ui.target.PickupTargetSelectRoute
import com.example.pickmeup.ui.terms.TermsAgreementRoute
import com.example.pickmeup.ui.terms.TermsDetailScreen
import com.example.pickmeup.ui.terms.TermsType

/**
 * 앱 전체 화면 이동.
 *
 * @param startWithTerms 약관에 아직 동의하지 않았으면 true → 약관 동의 화면부터 시작
 * @param onExit 약관 미동의 시 앱 종료
 */
@Composable
fun PickMeUpNavHost(
    startWithTerms: Boolean,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val comingSoon = stringResource(R.string.coming_soon)
    // TODO: Pleos 에서는 Toast 가 보이지 않음 → 화면 내 안내로 교체
    val showComingSoon = { Toast.makeText(context, comingSoon, Toast.LENGTH_SHORT).show() }

    NavHost(
        navController = navController,
        startDestination = if (startWithTerms) TermsAgreementDestination else MainDestination,
        modifier = modifier,
        // 차량 화면: 전환 애니메이션 없이 바로 바뀜
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable<TermsAgreementDestination> {
            TermsAgreementRoute(
                onAgreed = {
                    navController.navigate(MainDestination) {
                        popUpTo<TermsAgreementDestination> { inclusive = true }
                    }
                },
                onDisagree = onExit,
                onViewDetail = { navController.navigate(TermsDetailDestination(it)) },
            )
        }

        composable<TermsDetailDestination> { entry ->
            TermsDetailScreen(
                type = entry.toRoute<TermsDetailDestination>().type,
                onBack = navController.backFrom(entry),
            )
        }

        composable<MainDestination> {
            MainScreen(
                onSettingsClick = { navController.navigateSingle(SettingsDestination) },
                onAppGuideClick = { navController.navigateSingle(AppGuideDestination) },
                onRequestLocationClick = { navController.navigateSingle(LocationRequestDestination()) },
                onLoadPickupTargetsClick = { navController.navigateSingle(PickupTargetSelectDestination) },
            )
        }

        composable<PickupTargetSelectDestination> { entry ->
            PickupTargetSelectRoute(
                onBack = navController.backFrom(entry),
                onAppGuideClick = { navController.navigateSingle(AppGuideDestination) },
                onSettingsClick = { navController.navigateSingle(SettingsDestination) },
                onTargetSelected = { target ->
                    navController.navigate(LocationRequestDestination(target.name, target.phoneDigits))
                },
                onManualInputClick = { navController.navigate(LocationRequestDestination()) },
                onViewAllClick = showComingSoon, // TODO: 2-1-1 전체 프로필 관리 (별도 이슈)
            )
        }

        composable<LocationRequestDestination> { entry ->
            LocationRequestRoute(
                onBack = navController.backFrom(entry),
                onSettingsClick = { navController.navigateSingle(SettingsDestination) },
                onResponseReceived = { navController.navigateSingle(ShareResultDestination) },
            )
        }

        composable<ShareResultDestination> { entry ->
            LocationShareResultRoute(
                onBack = navController.backFrom(entry),
                onSettingsClick = { navController.navigateSingle(SettingsDestination) },
                onPickupStarted = { navController.navigateSingle(PickupInProgressDestination) },
            )
        }

        composable<PickupInProgressDestination> { entry ->
            PickupInProgressRoute(
                onBack = navController.backFrom(entry),
                onSettingsClick = { navController.navigateSingle(SettingsDestination) },
                // 픽업 종료: 메인만 남기고 모두 닫음 (메인에서 뒤로 가도 지난 픽업 화면이 안 나옴)
                onFinished = { navController.popBackStack(MainDestination, inclusive = false) },
            )
        }

        composable<AppGuideDestination> { entry ->
            AppGuideScreen(
                onBack = navController.backFrom(entry),
                onSettingsClick = { navController.navigateSingle(SettingsDestination) },
            )
        }

        composable<SettingsDestination> { entry ->
            SettingsScreen(
                onBack = navController.backFrom(entry),
                onAppGuideClick = { navController.navigateSingle(AppGuideDestination) },
                onPickupTargetClick = showComingSoon, // TODO: 픽업 대상자 지정 화면 (Figma 미정)
                onLocationTermsClick = { navController.navigate(TermsDetailDestination(TermsType.Location)) },
                onPrivacyPolicyClick = { navController.navigate(TermsDetailDestination(TermsType.Privacy)) },
            )
        }
    }
}

/**
 * 이미 스택에 있는 화면이면 그 화면까지 되돌아가고, 없으면 새로 엽니다.
 * (설정 → 가이드 → 설정 → … 처럼 스택이 끝없이 쌓이는 것 방지)
 */
private fun NavController.navigateSingle(route: Any) {
    if (!popBackStack(route, inclusive = false)) navigate(route)
}

/**
 * 뒤로 가기. 해당 화면이 현재 화면일 때만 동작해서
 * 버튼 연타·자동 뒤로 가기가 겹쳐도 화면이 두 번 닫히지 않습니다.
 */
private fun NavController.backFrom(entry: NavBackStackEntry): () -> Unit = {
    if (entry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) popBackStack()
}
