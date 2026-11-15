package com.example.pickmeup.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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

/**
 * 픽업 대상자 상황 알림 카드 (Figma 6-1-4-1~3 "픽업 대상자 상황 알림 / TTS 카드", 1310×342px).
 * 픽업 진행 중 화면 위쪽에 겹쳐 뜨는 비차단형 알림.
 *
 * Figma 절대 좌표(배지 y 31, 문구 y 137, 구분선 y 263, 안내 y 280)를 세로 배치로 옮겨서,
 * 창이 좁으면(화면 3분할) 카드 폭이 줄고 문구가 두 줄까지 늘어나도 겹치지 않습니다.
 */
@Composable
internal fun PickupMessageCard(message: PickupMessage, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(36.px)
    Column(
        modifier
            .widthIn(max = 1310.px)
            .fillMaxWidth()
            .heightIn(min = 342.px)
            .dropShadow(shape, Shadow(radius = 32.px, offset = DpOffset(0.px, 14.px), color = Color(0x38000000)))
            .clip(shape)
            .background(CardBg)
            .border(3.px, Purple, shape)
            .padding(start = 36.px, end = 38.px, top = 31.px, bottom = 34.px),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Badge(
                text = stringResource(R.string.pickup_message_badge),
                background = Purple,
                fontSize = 24,
                lineHeight = 28,
                radius = 24,
                modifier = Modifier.size(width = 226.px, height = 56.px),
            )
            Spacer(Modifier.weight(1f))
            Badge(
                text = stringResource(R.string.pickup_message_tts_badge),
                background = TtsBadgeBg,
                fontSize = 20,
                lineHeight = 24,
                radius = 22,
                modifier = Modifier
                    .padding(top = 5.px, end = 8.px)
                    .size(width = 222.px, height = 47.px),
            )
        }
        Spacer(Modifier.height(50.px))
        Text(
            text = stringResource(message.textRes),
            style = astaStyle(48.pxSp, FontWeight.Bold, Color.White, 64f / 48f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.px),
        )
        Spacer(Modifier.height(62.px))
        Box(
            Modifier
                .fillMaxWidth()
                .padding(start = 1.px)
                .height(2.px)
                .background(Divider),
        )
        Spacer(Modifier.height(15.px))
        Text(
            text = stringResource(R.string.pickup_message_footer),
            style = astaStyle(24.pxSp, FontWeight.Normal, FooterText, 28f / 24f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 3.px),
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

@Preview(name = "3분할", widthDp = 840, heightDp = 600, showBackground = true)
@Composable
private fun PickupMessageCardCompactPreview() {
    ProvideDesignScale(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().padding(horizontal = 64.px), contentAlignment = Alignment.Center) {
            PickupMessageCard(PickupMessage.Waiting)
        }
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
