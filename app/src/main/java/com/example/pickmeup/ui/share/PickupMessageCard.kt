package com.example.pickmeup.ui.share

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import com.example.pickmeup.R
import com.example.pickmeup.model.PickupMessage
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.theme.astaStyle

private val CardBg = Color(0xFF201D2C)
private val Purple = Color(0xFF5A46FA)
private val TtsBadgeBg = Color(0xFF383347)
private val Divider = Color(0xFF555064)
private val FooterText = Color(0xD6FFFFFF)  // rgba(255,255,255,0.84)

@StringRes
private fun PickupMessage.textRes(): Int = when (this) {
    PickupMessage.GoingToPlace -> R.string.pickup_message_going
    PickupMessage.AlmostArrived -> R.string.pickup_message_almost
    PickupMessage.Waiting -> R.string.pickup_message_waiting
}

/**
 * 픽업 대상자 상황 알림 카드 (Figma 6-1-4-1~3 "픽업 대상자 상황 알림 / TTS 카드", 1310×342px).
 * 픽업 진행 중 화면 위쪽에 겹쳐 뜨는 비차단형 알림. 위치는 Figma 절대 좌표 그대로입니다.
 */
@Composable
internal fun PickupMessageCard(message: PickupMessage, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(36.px)
    Box(
        modifier
            .size(width = 1310.px, height = 342.px)
            .dropShadow(shape, Shadow(radius = 32.px, offset = DpOffset(0.px, 14.px), color = Color(0x38000000)))
            .clip(shape)
            .background(CardBg)
            .border(3.px, Purple, shape),
    ) {
        Badge(
            text = stringResource(R.string.pickup_message_badge),
            background = Purple,
            fontSize = 24,
            lineHeight = 28,
            radius = 24,
            modifier = Modifier
                .offset(x = 36.px, y = 31.px)
                .size(width = 226.px, height = 56.px),
        )
        Badge(
            text = stringResource(R.string.pickup_message_tts_badge),
            background = TtsBadgeBg,
            fontSize = 20,
            lineHeight = 24,
            radius = 22,
            modifier = Modifier
                .offset(x = 1042.px, y = 36.px)
                .size(width = 222.px, height = 47.px),
        )
        Text(
            text = stringResource(message.textRes()),
            style = astaStyle(48.pxSp, FontWeight.Bold, Color.White, 64f / 48f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .offset(x = 42.px, y = 137.px)
                .width(1230.px),
        )
        Box(
            Modifier
                .offset(x = 37.px, y = 263.px)
                .size(width = 1235.px, height = 2.px)
                .background(Divider),
        )
        Text(
            text = stringResource(R.string.pickup_message_footer),
            style = astaStyle(24.pxSp, FontWeight.Normal, FooterText, 28f / 24f),
            modifier = Modifier
                .offset(x = 39.px, y = 280.px)
                .width(1220.px),
        )
    }
}

/** 둥근 라벨 ("대상자 메시지", "TTS 자동 안내") */
@Composable
private fun Badge(
    text: String,
    background: Color,
    fontSize: Int,
    lineHeight: Int,
    radius: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(radius.px))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = astaStyle(fontSize.pxSp, FontWeight.Bold, Color.White, lineHeight.toFloat() / fontSize),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(widthDp = 1700, heightDp = 600, showBackground = true)
@Composable
private fun PickupMessageCardPreview() {
    ProvideDesignScale(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            PickupMessageCard(PickupMessage.GoingToPlace)
        }
    }
}
