package com.example.pickmeup.ui.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickmeup.R
import com.example.pickmeup.model.LocationShareResult

@Composable
fun LocationShareResultRoute(
    onBack: () -> Unit,
    onAppGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPickupStarted: () -> Unit,
    viewModel: ShareResultViewModel = hiltViewModel(),
) {
    val result by viewModel.shareResult.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.pickupStarted.collect { onPickupStarted() } }

    val current = result
    if (current == null) {
        // 결과 없이 들어온 경우(비정상) 이전 화면으로
        LaunchedEffect(Unit) { onBack() }
        return
    }
    LocationShareResultScreen(
        result = current,
        onBack = onBack,
        onAppGuideClick = onAppGuideClick,
        onSettingsClick = onSettingsClick,
        onSetWaypoint = viewModel::setAsWaypoint,
        onSetDestination = viewModel::setAsDestination,
    )
}

/**
 * 위치 공유 결과 화면 — "경유지 / 목적지 선택".
 * - 허용: Figma 130:11560 "경유지 / 도착지 설정"
 * - 미허용: Figma 133:12536 "application - 미허용"
 */
@Composable
fun LocationShareResultScreen(
    result: LocationShareResult,
    onBack: () -> Unit,
    onAppGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSetWaypoint: (LocationShareResult.Allowed) -> Unit,
    onSetDestination: (LocationShareResult.Allowed) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAllowed = result is LocationShareResult.Allowed
    ShareScreenScaffold(
        // 미허용이면 뒤로 가기가 곧 위치 요청으로 돌아가는 것이라 제목도 그렇게 (Figma 6-2-1)
        title = stringResource(if (isAllowed) R.string.share_result_title else R.string.share_result_denied_title),
        onBack = onBack,
        onSettingsClick = onSettingsClick,
        modifier = modifier,
        // Figma: 미허용 화면에는 가이드(책) 버튼 없음
        onAppGuideClick = if (isAllowed) onAppGuideClick else null,
    ) {
        when (result) {
            is LocationShareResult.Allowed -> LocationStatusCard(
                title = stringResource(R.string.share_result_allowed),
                address = result.address,
                guide = stringResource(R.string.share_result_question),
            ) {
                CardButton(
                    text = stringResource(R.string.share_result_set_waypoint),
                    onClick = { onSetWaypoint(result) },
                    modifier = Modifier.weight(1f),
                )
                CardButton(
                    text = stringResource(R.string.share_result_set_destination),
                    onClick = { onSetDestination(result) },
                    modifier = Modifier.weight(1f),
                )
            }

            is LocationShareResult.Denied -> MessageCard(stringResource(R.string.share_result_denied))
        }
    }
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun AllowedPreview() {
    LocationShareResultScreen(
        result = LocationShareResult.Allowed("홍길동", "00시 00구 00로", 0.0, 0.0),
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {}, onSetWaypoint = {}, onSetDestination = {},
    )
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun DeniedPreview() {
    LocationShareResultScreen(
        result = LocationShareResult.Denied("홍길동"),
        onBack = {}, onAppGuideClick = {}, onSettingsClick = {}, onSetWaypoint = {}, onSetDestination = {},
    )
}
