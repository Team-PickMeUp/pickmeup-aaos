package com.example.pickmeup.ui.location

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import com.example.pickmeup.R
import com.example.pickmeup.ui.components.DimOverlay
import com.example.pickmeup.ui.components.consumeClicks
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.popupWidth
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

private val CancelRed = Color(0xFFFF4C28)
private val ConfirmPurple = Color(0xFF5A46F7)
private val CountdownPurple = Color(0xFF4D3DF2)
private val CaptionGray = Color(0xFF747884)
private val StatusCircle = Color(0x1AFF4C28) // #FF4C28 10%

/**
 * 위치 공유 요청 상태에 맞는 팝업을 화면 위에 띄웁니다.
 * - 대기 중: [LocationShareWaitingDialog] (뒤로 가기 = 요청 취소)
 * - 자동 취소 / 거부: [LocationShareEndedDialog] (뒤로 가기 = 확인)
 *
 * 위치 요청 화면과 픽업 불러오기 화면(Figma 2-1-4-1)에서 함께 씁니다.
 */
@Composable
internal fun LocationShareRequestOverlay(
    state: LocationShareRequestState,
    onCancelRequest: () -> Unit,
    onDismissResult: () -> Unit,
) {
    when (state) {
        LocationShareRequestState.Idle -> Unit
        is LocationShareRequestState.Waiting -> {
            BackHandler(onBack = onCancelRequest)
            LocationShareWaitingDialog(remainingSeconds = state.remainingSeconds, onCancel = onCancelRequest)
        }
        LocationShareRequestState.TimedOut -> {
            BackHandler(onBack = onDismissResult)
            LocationShareEndedDialog(
                icon = R.drawable.ic_share_timeout,
                title = R.string.share_timeout_title,
                description = R.string.share_timeout_desc,
                onConfirm = onDismissResult,
            )
        }
        LocationShareRequestState.Denied -> {
            BackHandler(onBack = onDismissResult)
            LocationShareEndedDialog(
                icon = R.drawable.ic_share_denied,
                title = R.string.share_denied_title,
                description = R.string.share_denied_desc,
                onConfirm = onDismissResult,
            )
        }
    }
}

/** 560×629px 팝업 틀 (대기 / 자동 취소 / 거부 공통) */
@Composable
private fun SharePopup(content: @Composable () -> Unit) {
    DimOverlay {
        val shape = RoundedCornerShape(36.px)
        Box(
            Modifier
                .popupWidth(560)
                .height(629.px)
                .dropShadow(shape, Shadow(radius = 40.px, offset = DpOffset(0.px, 20.px), color = Color(0x33000000)))
                .clip(shape)
                .background(Color.White)
                .consumeClicks(),
        ) {
            content()
        }
    }
}

/**
 * 위치 공유 대기 팝업 (Figma 270:16766 "팝업 / 위치 공유 대기").
 * 바깥을 눌러도 닫히지 않고, "요청 취소"(또는 뒤로 가기)로만 닫힙니다.
 * 5분이 지나면 [LocationShareRequester] 가 자동으로 취소합니다.
 */
@Composable
internal fun LocationShareWaitingDialog(remainingSeconds: Int, onCancel: () -> Unit) {
    SharePopup {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 61.5.px),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.waiting_title),
                style = astaStyle(40.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 1f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(62.5.px))
            Text(
                text = stringResource(R.string.waiting_desc),
                style = astaStyle(24.pxSp, FontWeight.Normal, PickMeUpColors.TextStrong, 28f / 24f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(65.5.px))
            Text(
                text = stringResource(R.string.waiting_timeout_notice),
                style = astaStyle(22.pxSp, FontWeight.Normal, CaptionGray, 29f / 22f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(38.px))
            // Figma: "남은 시간" 아래로 카운트다운이 8.5px 겹쳐 있음
            Box(Modifier.height(87.5.px), contentAlignment = Alignment.TopCenter) {
                Text(
                    text = stringResource(R.string.waiting_remaining_label),
                    style = astaStyle(21.pxSp, FontWeight.Normal, CaptionGray, 28f / 21f),
                    textAlign = TextAlign.Center,
                )
                val time = formatRemaining(remainingSeconds)
                val timeDescription = stringResource(
                    R.string.waiting_remaining_description,
                    remainingSeconds / 60,
                    remainingSeconds % 60,
                )
                Text(
                    text = time,
                    style = astaStyle(48.pxSp, FontWeight.Bold, CountdownPurple, 64f / 48f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 21.5.px)
                        .semantics { contentDescription = timeDescription },
                )
            }
            Spacer(Modifier.height(29.px))
            PopupButton(
                text = stringResource(R.string.waiting_cancel),
                background = CancelRed,
                onClick = onCancel,
            )
        }
    }
}

/**
 * 위치 공유 요청이 끝났음을 알리는 팝업.
 * - 자동 취소: Figma 758:29295 "팝업 / 응답 없음 자동 취소" (4-2)
 * - 거부: Figma 758:30439 "팝업 / 위치 공유 거부" (4-3)
 * 확인을 누르면 팝업만 닫히고 원래 화면(입력값 유지)에 머뭅니다.
 */
@Composable
internal fun LocationShareEndedDialog(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes description: Int,
    onConfirm: () -> Unit,
) {
    SharePopup {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 75.px),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(88.px)
                    .clip(CircleShape)
                    .background(StatusCircle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(48.px),
                )
            }
            // 제목 영역 166px 가운데에 두 줄(104px)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(166.px),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(title),
                    style = astaStyle(40.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 52f / 40f),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(18.px))
            Text(
                text = stringResource(description),
                style = astaStyle(24.pxSp, FontWeight.Normal, PickMeUpColors.TextStrong, 28f / 24f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(86.px))
            PopupButton(
                text = stringResource(R.string.common_confirm),
                background = ConfirmPurple,
                onClick = onConfirm,
            )
        }
    }
}

/** 팝업 하단 380×75px 버튼 */
@Composable
private fun PopupButton(text: String, background: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .size(width = 380.px, height = 75.px)
            .clip(RoundedCornerShape(20.px))
            .background(background)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = astaStyle(26.pxSp, FontWeight.Bold, Color.White, 34f / 26f),
        )
    }
}

/** 남은 초 → "04:59" */
internal fun formatRemaining(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    return "%02d:%02d".format(safe / 60, safe % 60)
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun WaitingDialogPreview() {
    ProvideDesignScale(Modifier.fillMaxSize()) {
        LocationShareWaitingDialog(remainingSeconds = 300, onCancel = {})
    }
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun TimedOutDialogPreview() {
    ProvideDesignScale(Modifier.fillMaxSize()) {
        LocationShareRequestOverlay(LocationShareRequestState.TimedOut, onCancelRequest = {}, onDismissResult = {})
    }
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun DeniedDialogPreview() {
    ProvideDesignScale(Modifier.fillMaxSize()) {
        LocationShareRequestOverlay(LocationShareRequestState.Denied, onCancelRequest = {}, onDismissResult = {})
    }
}
