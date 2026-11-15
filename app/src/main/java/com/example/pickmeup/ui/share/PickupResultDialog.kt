package com.example.pickmeup.ui.share

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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

private val Purple = Color(0xFF5A46FA)
private val CancelledCircle = Color(0x1AFF4C28) // #FF4C28 10%
private val CompletedCircle = Color(0xFFF0EDFF)
private val CompletedCheck = Color(0xFF02C265)

/**
 * 픽업 종료 결과 팝업 (Figma 8-1 "픽업이 취소되었습니다" / 8-2 "픽업이 완료되었습니다", 750×520px).
 * 이미 처리된 결과를 알리는 팝업이라 바깥을 눌러도 닫히지 않고, 확인을 눌러야 첫 화면(픽업 불러오기)으로 갑니다.
 */
@Composable
internal fun PickupResultDialog(result: PickupResult, onConfirm: () -> Unit) {
    val content = when (result) {
        PickupResult.Cancelled -> ResultContent(
            title = R.string.pickup_result_cancelled_title,
            description = R.string.pickup_result_cancelled_desc,
            circle = CancelledCircle,
            descriptionGap = 11,
        )
        PickupResult.Completed -> ResultContent(
            title = R.string.pickup_result_completed_title,
            description = R.string.pickup_result_completed_desc,
            circle = CompletedCircle,
            // Figma 8-2: 안내 문구가 8-1 보다 16px 아래 (top 273 vs 257)
            descriptionGap = 27,
        )
    }
    DimOverlay {
        val shape = RoundedCornerShape(36.px)
        Column(
            Modifier
                .popupWidth(750)
                .height(520.px)
                .dropShadow(shape, Shadow(radius = 36.px, offset = DpOffset(0.px, 12.px), color = Color(0x38000000)))
                .clip(shape)
                .background(Color.White)
                .consumeClicks()
                .padding(top = 54.px),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(100.px)
                    .clip(CircleShape)
                    .background(content.circle),
                contentAlignment = Alignment.Center,
            ) {
                when (result) {
                    PickupResult.Cancelled -> Icon(
                        painter = painterResource(R.drawable.ic_pickup_cancelled),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(74.px),
                    )
                    // Figma 도 글자(✓)로 그린 아이콘
                    PickupResult.Completed -> Text(
                        text = "✓",
                        style = astaStyle(53.pxSp, FontWeight.Bold, CompletedCheck, 72f / 53f),
                    )
                }
            }
            Spacer(Modifier.height(28.px))
            Text(
                text = stringResource(content.title),
                style = astaStyle(48.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 64f / 48f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(content.descriptionGap.px))
            Text(
                text = stringResource(content.description),
                style = astaStyle(26.pxSp, FontWeight.Normal, PickMeUpColors.TextStrong, 34f / 26f),
                textAlign = TextAlign.Center,
            )
            // 버튼 top 382px 고정 (안내 문구 위치와 무관)
            Spacer(Modifier.height((57 + 11 - content.descriptionGap).px))
            Box(
                Modifier
                    .padding(horizontal = 40.px)
                    .widthIn(max = 540.px)
                    .fillMaxWidth()
                    .height(100.px)
                    .clip(RoundedCornerShape(32.px))
                    .background(Purple)
                    .clickable(role = Role.Button, onClick = onConfirm),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.common_confirm),
                    style = astaStyle(32.pxSp, FontWeight.Bold, Color.White, 40f / 32f),
                )
            }
        }
    }
}

private data class ResultContent(
    @StringRes val title: Int,
    @StringRes val description: Int,
    val circle: Color,
    /** 제목과 안내 문구 사이 간격(px) */
    val descriptionGap: Int,
)

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupCancelledDialogPreview() {
    ProvideDesignScale(Modifier.fillMaxSize()) {
        PickupResultDialog(result = PickupResult.Cancelled, onConfirm = {})
    }
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun PickupCompletedDialogPreview() {
    ProvideDesignScale(Modifier.fillMaxSize()) {
        PickupResultDialog(result = PickupResult.Completed, onConfirm = {})
    }
}
