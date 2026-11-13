package com.example.pickmeup.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.pickmeup.ui.components.SettingsButton
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

private val ButtonPurple = Color(0xFF5A46FA)
private val ButtonPurpleBorder = Color(0xFF8F80F5)

/**
 * 메인 화면 (Figma node 18:21218 "메인화면").
 * 디자인의 절대 위치를 화면 중앙 기준 오프셋으로 옮겼습니다.
 *  - 버튼1 중심 = 화면 중앙, 버튼2 중심 = 중앙 +256px
 *  - 문구 중심 = y 323px (중앙 592px 기준 -269px)
 */
@Composable
fun MainScreen(
    onSettingsClick: () -> Unit,
    onRequestLocationClick: () -> Unit,
    onAppGuideClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProvideDesignScale(
        modifier
            .fillMaxSize()
            .background(PickMeUpColors.BackgroundPrimary),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SettingsButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 23.px, end = 23.px),
            )

            Text(
                text = stringResource(R.string.main_greeting),
                style = astaStyle(60.pxSp, FontWeight.Bold, Color.Black, lineHeightMultiplier = 80f / 60f),
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-269).px),
            )

            MainActionButton(
                text = stringResource(R.string.main_request_location),
                onClick = onRequestLocationClick,
                bordered = false,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_map_pin),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(52.px),
                )
            }

            MainActionButton(
                text = stringResource(R.string.main_app_guide),
                onClick = onAppGuideClick,
                bordered = true,
                modifier = Modifier.offset(y = 256.px),
            ) {
                GuideIcon()
            }
        }
    }
}

/** 580×228px 보라색 메인 버튼 (Shadow/Light/Level2) */
@Composable
private fun MainActionButton(
    text: String,
    onClick: () -> Unit,
    bordered: Boolean,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(32.px)
    Row(
        modifier
            .size(width = 580.px, height = 228.px)
            .dropShadow(shape, Shadow(radius = 32.px, offset = DpOffset(0.px, 8.px), color = Color(0x29000000)))
            .dropShadow(shape, Shadow(radius = 2.px, offset = DpOffset(0.px, 2.px), color = Color(0x14000000)))
            .clip(shape)
            .background(ButtonPurple)
            .then(if (bordered) Modifier.border(1.px, ButtonPurpleBorder, shape) else Modifier)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.px),
        horizontalArrangement = Arrangement.spacedBy(12.px, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Text(
            text = text,
            style = astaStyle(60.pxSp, FontWeight.ExtraBold, Color.White, lineHeightMultiplier = 80f / 60f),
        )
    }
}

/** Figma guide-icon: 52px 박스 안에 원(48px, 2px inset) + "?" 글자 */
@Composable
private fun GuideIcon() {
    Box(Modifier.size(52.px), contentAlignment = Alignment.Center) {
        Icon(
            painter = painterResource(R.drawable.ic_help_circle),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(48.px),
        )
        Text(
            text = "?",
            style = astaStyle(32.pxSp, FontWeight.Bold, Color.White),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun MainScreenPreview() {
    MainScreen(onSettingsClick = {}, onRequestLocationClick = {}, onAppGuideClick = {})
}
