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
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.pickupStarted.collect { onPickupStarted() } }

    val current = result
    if (current == null) {
        // 결과 없이 들어온 경우(비정상) 이전 화면으로
        LaunchedEffect(Unit) { onBack() }
        return
    }
    LocationShareResultScreen(
        result = current,
        enabled = !isProcessing,
        onAppGuideClick = onAppGuideClick,
        onSettingsClick = onSettingsClick,
        onSetWaypoint = viewModel::setAsWaypoint,
        onSetDestination = viewModel::setAsDestination,
    )
}

/**
 * 경유지 / 목적지 선택 (Figma 130:11560 "7 경유지 / 도착지 설정").
 * 대상자가 위치 공유를 허용했을 때만 들어옵니다. 거부는 위치 요청 화면의 팝업(Figma 4-3)으로 처리합니다.
 * 내비게이션에 위치를 넘긴 뒤의 화면이라 뒤로 가기 버튼이 없습니다.
 *
 * 버튼을 누르면 NaviHelper 로 내비에 경유지/목적지를 넘기고, 내비가 처리하면 픽업 진행 중 화면으로 갑니다.
 * 내비 안내 중이 아닐 때 "경유지로 설정하기"는 목적지로 설정됩니다 (경유지는 목적지가 있어야 추가 가능).
 */
@Composable
fun LocationShareResultScreen(
    result: LocationShareResult.Allowed,
    onAppGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSetWaypoint: (LocationShareResult.Allowed) -> Unit,
    onSetDestination: (LocationShareResult.Allowed) -> Unit,
    modifier: Modifier = Modifier,
    /** false 면 내비 처리 중 → 버튼 비활성 */
    enabled: Boolean = true,
) {
    ShareScreenScaffold(
        onSettingsClick = onSettingsClick,
        modifier = modifier,
        onAppGuideClick = onAppGuideClick,
    ) {
        LocationStatusCard(
            title = stringResource(R.string.share_result_allowed),
            address = result.address,
            guide = stringResource(R.string.share_result_question),
        ) {
            CardButton(
                text = stringResource(R.string.share_result_set_waypoint),
                onClick = { onSetWaypoint(result) },
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
            CardButton(
                text = stringResource(R.string.share_result_set_destination),
                onClick = { onSetDestination(result) },
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun AllowedPreview() {
    LocationShareResultScreen(
        result = LocationShareResult.Allowed("홍길동", "00시 00구 00로", 0.0, 0.0),
        onAppGuideClick = {}, onSettingsClick = {}, onSetWaypoint = {}, onSetDestination = {},
    )
}
