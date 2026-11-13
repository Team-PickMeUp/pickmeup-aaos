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
import com.example.pickmeup.ui.components.SettingsButton
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

private val Purple = Color(0xFF5A46FA)
private val CardBorder = Color(0xFFDADCE3)

/**
 * 위치 공유 이후 화면들의 공통 틀: 작은 헤더(뒤로 + 제목) + 오른쪽 위 설정 버튼.
 * Figma: 경유지 / 도착지 설정(130:11560), 미허용(133:12536), 픽업 진행 중(261:15639)
 */
@Composable
internal fun ShareScreenScaffold(
    title: String,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
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
                    .padding(horizontal = 80.px, vertical = 48.px),
                verticalArrangement = Arrangement.spacedBy(48.px),
            ) {
                Row(
                    Modifier
                        .height(48.px)
                        .clickable(role = Role.Button, onClick = onBack),
                    horizontalArrangement = Arrangement.spacedBy(16.px),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_left),
                        contentDescription = stringResource(R.string.common_back),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(48.px),
                    )
                    Text(
                        text = title,
                        style = astaStyle(32.pxSp, FontWeight.SemiBold, PickMeUpColors.TextPrimary, 40f / 32f),
                    )
                }
                content()
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
 * 950px 상태 카드: 제목 / 위치(가운데) / 안내 문구 / 버튼 2개.
 * 본문 618px 안에서 위치는 top 249.5px, 안내 문구는 top 546px (Figma 절대 위치 그대로).
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
            .height(950.px)
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
                    .padding(top = 249.5.px),
            )
            Text(
                text = guide,
                style = astaStyle(60.pxSp, FontWeight.Normal, PickMeUpColors.TextPrimary),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 546.px),
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(40.px),
            content = buttons,
        )
    }
}

/** 미허용 등 한 줄 메시지 카드 */
@Composable
internal fun MessageCard(text: String) {
    val shape = RoundedCornerShape(24.px)
    Text(
        text = text,
        style = astaStyle(60.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 80f / 60f),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(1.px, CardBorder, shape)
            .padding(36.px),
    )
}

/**
 * 카드 하단 100px 버튼.
 * - primary: 보라 배경 + 흰 글자
 * - secondary: 흰 배경 + 2px #DADCE3 테두리 + 진한 글자 (Figma "픽업 취소 버튼")
 */
@Composable
internal fun CardButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = true,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(16.px)
    Box(
        modifier
            .height(100.px)
            .clip(shape)
            .background(if (primary) Purple else Color.White)
            .then(if (primary) Modifier else Modifier.border(2.px, CardBorder, shape))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.px),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = astaStyle(
                34.pxSp,
                FontWeight.Bold,
                if (primary) Color.White else PickMeUpColors.TextPrimary,
                44f / 34f,
            ),
            softWrap = false,
        )
    }
}
