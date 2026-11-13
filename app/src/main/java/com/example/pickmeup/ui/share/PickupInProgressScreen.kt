package com.example.pickmeup.ui.share

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pickmeup.R

/**
 * 픽업 진행 중 화면 (Figma 261:15639 "픽업 진행 중 (경유지/목적지 설정 후)").
 * 경유지 또는 목적지로 설정한 뒤 표시되며, 공유받은 위치를 계속 보여줍니다.
 */
@Composable
fun PickupInProgressScreen(
    location: LocationShareResult.Allowed,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    onCancelPickup: () -> Unit,
    onCompletePickup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ShareScreenScaffold(
        title = stringResource(R.string.pickup_progress_title),
        onBack = onBack,
        onSettingsClick = onSettingsClick,
        modifier = modifier,
    ) {
        LocationStatusCard(
            title = stringResource(R.string.pickup_progress_heading),
            address = location.address,
            guide = stringResource(R.string.pickup_progress_guide),
        ) {
            CardButton(
                text = stringResource(R.string.pickup_cancel),
                onClick = onCancelPickup,
                primary = false,
                modifier = Modifier.weight(1f),
            )
            CardButton(
                text = stringResource(R.string.pickup_complete),
                onClick = onCompletePickup,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupInProgressPreview() {
    PickupInProgressScreen(
        location = LocationShareResult.Allowed("홍길동", "00시 00구 00로", 0.0, 0.0),
        onBack = {}, onSettingsClick = {}, onCancelPickup = {}, onCompletePickup = {},
    )
}
