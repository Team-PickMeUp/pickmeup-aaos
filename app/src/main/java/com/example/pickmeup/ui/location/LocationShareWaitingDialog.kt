package com.example.pickmeup.ui.location

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import com.example.pickmeup.R
import com.example.pickmeup.ui.components.DimOverlay
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

private val Purple = Color(0xFF5A46FA)
private val CancelRed = Color(0xFFFF4C28)

/**
 * 위치 공유 대기 팝업 (Figma 270:16683 "4 운전자 화면 · 픽업 대상자 위치 공유 대기 중").
 * 위치 요청 화면 전체를 어둡게 덮고 가운데에 560×629px 팝업을 띄웁니다.
 * 바깥을 눌러도 닫히지 않고, "요청 취소"(또는 뒤로 가기)로만 닫힙니다.
 */
@Composable
internal fun LocationShareWaitingDialog(onCancel: () -> Unit) {
    DimOverlay {
        val shape = RoundedCornerShape(36.px)
        Column(
            Modifier
                .size(width = 560.px, height = 629.px)
                .dropShadow(shape, Shadow(radius = 40.px, offset = DpOffset(0.px, 20.px), color = Color(0x33000000)))
                .clip(shape)
                .background(Color.White)
                .padding(start = 72.px, end = 72.px, top = 66.px, bottom = 58.px),
            verticalArrangement = Arrangement.spacedBy(30.px, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.waiting_title),
                style = astaStyle(40.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 52f / 40f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.waiting_desc),
                style = astaStyle(24.pxSp, FontWeight.Normal, PickMeUpColors.TextStrong, 28f / 24f),
                textAlign = TextAlign.Center,
            )
            LoadingSpinner()
            Box(
                Modifier
                    .size(width = 380.px, height = 75.px)
                    .clip(RoundedCornerShape(20.px))
                    .background(CancelRed)
                    .clickable(role = Role.Button, onClick = onCancel),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.waiting_cancel),
                    style = astaStyle(26.pxSp, FontWeight.Bold, Color.White, 34f / 26f),
                )
            }
        }
    }
}

/**
 * 로딩 아이콘 (Figma line-md:loading-twotone-loop, 48px).
 * 연한 원(30%) 위에서 보라색 1/4 호가 1초에 한 바퀴 돕니다.
 */
@Composable
private fun LoadingSpinner() {
    val rotation by rememberInfiniteTransition(label = "spinner").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1_000, easing = LinearEasing), RepeatMode.Restart),
        label = "spinnerRotation",
    )
    Canvas(
        Modifier
            .size(48.px)
            .rotate(rotation),
    ) {
        // Figma: 48px 안에 지름 36px 원, 선 두께 4px
        val scale = size.minDimension / 48f
        val stroke = Stroke(width = 4f * scale, cap = StrokeCap.Round)
        val diameter = 36f * scale
        val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
        val arcSize = Size(diameter, diameter)
        drawArc(Purple.copy(alpha = 0.3f), 0f, 360f, false, topLeft, arcSize, style = stroke)
        drawArc(Purple, -90f, 90f, false, topLeft, arcSize, style = stroke)
    }
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun LocationShareWaitingDialogPreview() {
    ProvideDesignScale(Modifier.fillMaxSize()) {
        LocationShareWaitingDialog(onCancel = {})
    }
}
