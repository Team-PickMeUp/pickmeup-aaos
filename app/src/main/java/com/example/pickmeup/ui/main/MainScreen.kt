package com.example.pickmeup.ui.main

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
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
import com.example.pickmeup.ui.components.GuideButton
import com.example.pickmeup.ui.components.SettingsButton
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

private val ButtonPurple = Color(0xFF5A46FA)

/**
 * 메인 화면 (Figma node 18:21218 "2 메인화면").
 * 디자인의 절대 위치를 화면 중앙 기준 오프셋으로 옮겼습니다.
 *  - 버튼1 중심 = 화면 중앙, 버튼2 중심 = 중앙 +256px
 *  - 문구 중심 = y 323px (중앙 592px 기준 -269px)
 *  - 오른쪽 위: 가이드(책) 버튼 right 101px, 설정 버튼 right 23px
 */
@Composable
fun MainScreen(
    onSettingsClick: () -> Unit,
    onAppGuideClick: () -> Unit,
    onRequestLocationClick: () -> Unit,
    onLoadPickupTargetsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProvideDesignScale(
        modifier
            .fillMaxSize()
            .background(PickMeUpColors.BackgroundPrimary),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            GuideButton(
                onClick = onAppGuideClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 23.px, end = 101.px),
            )
            SettingsButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 23.px, end = 23.px),
            )

            Text(
                text = stringResource(R.string.main_greeting),
                style = astaStyle(60.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, lineHeightMultiplier = 80f / 60f),
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-269).px),
            )

            MainActionButton(
                text = stringResource(R.string.main_request_location),
                icon = R.drawable.ic_map_pin,
                onClick = onRequestLocationClick,
            )

            MainActionButton(
                text = stringResource(R.string.main_load_pickup_targets),
                icon = R.drawable.ic_pickup_targets,
                onClick = onLoadPickupTargetsClick,
                modifier = Modifier.offset(y = 256.px),
            )
        }
    }
}

/** 580×228px 보라색 메인 버튼 (Shadow/Light/Level2) */
@Composable
private fun MainActionButton(
    text: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(32.px)
    Row(
        modifier
            .size(width = 580.px, height = 228.px)
            .dropShadow(shape, Shadow(radius = 32.px, offset = DpOffset(0.px, 8.px), color = Color(0x29000000)))
            .dropShadow(shape, Shadow(radius = 2.px, offset = DpOffset(0.px, 2.px), color = Color(0x14000000)))
            .clip(shape)
            .background(ButtonPurple)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.px),
        horizontalArrangement = Arrangement.spacedBy(12.px, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(52.px),
        )
        Text(
            text = text,
            style = astaStyle(60.pxSp, FontWeight.ExtraBold, Color.White, lineHeightMultiplier = 80f / 60f),
        )
    }
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun MainScreenPreview() {
    MainScreen(
        onSettingsClick = {}, onAppGuideClick = {},
        onRequestLocationClick = {}, onLoadPickupTargetsClick = {},
    )
}
