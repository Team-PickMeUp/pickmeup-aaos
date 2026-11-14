package com.example.pickmeup.ui.share

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickmeup.R
import com.example.pickmeup.model.LocationShareResult
import com.example.pickmeup.model.PickupMessage
import com.example.pickmeup.ui.design.px

@Composable
fun PickupInProgressRoute(
    onBack: () -> Unit,
    onAppGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFinished: () -> Unit,
    viewModel: PickupInProgressViewModel = hiltViewModel(),
) {
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    // 결과 팝업이 떠 있으면 뒤로 가기 = 확인 (픽업이 이미 끝나서 이전 화면으로 갈 수 없음)
    BackHandler(enabled = result != null, onBack = onFinished)

    val current = viewModel.location
    if (current == null) {
        // 진행 중인 픽업 없이 들어온 경우(비정상) 이전 화면으로
        LaunchedEffect(Unit) { onBack() }
        return
    }
    PickupInProgressScreen(
        location = current,
        result = result,
        onResultConfirm = onFinished,
        message = message,
        enabled = !isProcessing,
        onAppGuideClick = onAppGuideClick,
        onSettingsClick = onSettingsClick,
        onCancelPickup = viewModel::cancelPickup,
        onCompletePickup = viewModel::completePickup,
    )
}

/**
 * 픽업 진행 중 화면 (Figma 261:15639 "픽업 진행 중 (경유지/목적지 설정 후)").
 * 경유지 또는 목적지로 설정한 뒤 표시되며, 공유받은 위치를 계속 보여줍니다.
 * 픽업 취소·완료 처리 후 결과 팝업(Figma 8-1, 8-2) → 확인 시 첫 화면(픽업 불러오기)으로.
 * 대상자 상황 메시지가 오면 위쪽에 알림 카드(Figma 6-1-4-1~3).
 */
@Composable
fun PickupInProgressScreen(
    location: LocationShareResult.Allowed,
    onAppGuideClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCancelPickup: () -> Unit,
    onCompletePickup: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    result: PickupResult? = null,
    onResultConfirm: () -> Unit = {},
    message: PickupMessage? = null,
) {
    ShareScreenScaffold(
        onSettingsClick = onSettingsClick,
        modifier = modifier,
        onAppGuideClick = onAppGuideClick,
        overlay = {
            // 대상자 상황 알림: 카드 위쪽에 겹쳐 표시 (Figma: 화면 가운데, top 144px). 픽업이 끝나면 숨김
            if (message != null && result == null) {
                PickupMessageCard(
                    message = message,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 144.px),
                )
            }
            result?.let { PickupResultDialog(result = it, onConfirm = onResultConfirm) }
        },
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
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
            CardButton(
                text = stringResource(R.string.pickup_complete),
                onClick = onCompletePickup,
                enabled = enabled,
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
        onAppGuideClick = {}, onSettingsClick = {}, onCancelPickup = {}, onCompletePickup = {},
    )
}
