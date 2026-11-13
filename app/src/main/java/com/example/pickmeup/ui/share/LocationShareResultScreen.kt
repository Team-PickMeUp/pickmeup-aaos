package com.example.pickmeup.ui.share

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pickmeup.R

/**
 * 위치 공유 결과 화면 — "경유지 / 목적지 선택".
 * - 허용: Figma 130:11560 "경유지 / 도착지 설정"
 * - 미허용: Figma 133:12536 "application - 미허용"
 */
@Composable
fun LocationShareResultScreen(
    result: LocationShareResult,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    onSetWaypoint: (LocationShareResult.Allowed) -> Unit,
    onSetDestination: (LocationShareResult.Allowed) -> Unit,
    modifier: Modifier = Modifier,
) {
    ShareScreenScaffold(
        title = stringResource(R.string.share_result_title),
        onBack = onBack,
        onSettingsClick = onSettingsClick,
        modifier = modifier,
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
        onBack = {}, onSettingsClick = {}, onSetWaypoint = {}, onSetDestination = {},
    )
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun DeniedPreview() {
    LocationShareResultScreen(
        result = LocationShareResult.Denied("홍길동"),
        onBack = {}, onSettingsClick = {}, onSetWaypoint = {}, onSetDestination = {},
    )
}
