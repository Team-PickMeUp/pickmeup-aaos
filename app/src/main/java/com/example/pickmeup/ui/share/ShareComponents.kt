package com.example.pickmeup.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.pickmeup.R
import com.example.pickmeup.ui.components.GuideButton
import com.example.pickmeup.ui.components.SettingsButton
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

private val Purple = Color(0xFF5A46FA)
private val CardBorder = Color(0xFFDADCE3)
private val SecondaryButtonBg = Color(0x0D131417) // rgba(19,20,23,0.05)

/**
 * 위치 공유 이후 화면들의 공통 틀: 오른쪽 위 가이드·설정 버튼 + 화면을 채우는 카드.
 * Figma: 경유지 / 도착지 설정(130:11560), 픽업 진행 중(261:15639)
 *
 * 내비게이션에 위치를 넘긴 뒤의 화면이라 헤더(뒤로 가기)가 없습니다.
 * 카드는 앱 영역 (64, 128) 에서 1572×1016px 입니다.
 *
 * @param onAppGuideClick null 이면 가이드(책) 버튼 없음
 */
@Composable
internal fun ShareScreenScaffold(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAppGuideClick: (() -> Unit)? = null,
    /** 화면 전체 위에 덮는 팝업 등 (설정 버튼보다 위) */
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    ProvideDesignScale(
        modifier
            .fillMaxSize()
            .background(PickMeUpColors.BackgroundPrimary),
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(start = 64.px, end = 64.px, top = 128.px),
            ) {
                content()
            }

            if (onAppGuideClick != null) {
                GuideButton(
                    onClick = onAppGuideClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 23.px, end = 101.px),
                )
            }
            SettingsButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 23.px, end = 23.px),
            )
            overlay()
        }
    }
}

/**
 * 1016px 상태 카드: 제목 / 위치(가운데) / 안내 문구 / 버튼 2개.
 * 본문 684px 안에서 위치는 top 282.5px(가운데 - 59.5), 안내 문구는 top 604px(아래에서 80) — Figma 절대 위치 그대로.
 */
@Composable
internal fun LocationStatusCard(
    title: String,
    address: String,
    guide: String,
    buttons: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(24.px)
    Column(
        Modifier
            .fillMaxWidth()
            .height(1016.px)
            .clip(shape)
            .background(Color.White)
            .border(1.px, CardBorder, shape)
            .padding(36.px),
        verticalArrangement = Arrangement.spacedBy(40.px),
    ) {
        Text(
            text = title,
            style = astaStyle(60.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 80f / 60f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Text(
                text = stringResource(R.string.share_result_location, address),
                style = astaStyle(60.pxSp, FontWeight.ExtraBold, PickMeUpColors.TextPrimary, 80f / 60f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 282.5.px),
            )
            Text(
                text = guide,
                style = astaStyle(60.pxSp, FontWeight.Normal, PickMeUpColors.TextPrimary),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 604.px),
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(40.px),
            content = buttons,
        )
    }
}

/**
 * 카드 하단 100px 버튼 (모서리 32px, 글자 32px).
 * - primary: 보라 배경 + 흰 글자
 * - secondary: 연회색 배경(rgba(19,20,23,0.05)) + 진한 글자 (Figma "픽업 취소 버튼")
 */
@Composable
internal fun CardButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = true,
    enabled: Boolean = true,
) {
    Box(
        modifier
            .height(100.px)
            .clip(RoundedCornerShape(32.px))
            .background(if (primary) Purple else SecondaryButtonBg)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.px),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = astaStyle(
                32.pxSp,
                FontWeight.Bold,
                if (primary) Color.White else PickMeUpColors.TextPrimary,
                40f / 32f,
            ),
            softWrap = false,
        )
    }
}
