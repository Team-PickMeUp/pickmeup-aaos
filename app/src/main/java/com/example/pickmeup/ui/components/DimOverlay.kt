package com.example.pickmeup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** Figma 팝업 딤 대부분: 검정 44% */
val DefaultDim = Color(0x70000000)

/**
 * 앱 화면 전체를 어둡게 덮고 가운데에 팝업을 띄우는 틀.
 * 아래 화면은 눌리지 않습니다.
 *
 * @param onOutsideClick 바깥(딤)을 눌렀을 때. null 이면 바깥을 눌러도 아무 일 없음
 * @param content 팝업. 팝업 안을 눌렀을 때 바깥 클릭으로 처리되지 않도록 Modifier.consumeClicks() 를 붙여 주세요
 */
@Composable
fun DimOverlay(
    modifier: Modifier = Modifier,
    dimColor: Color = DefaultDim,
    onOutsideClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(dimColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onOutsideClick ?: {},
            ),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/** 팝업 안을 눌러도 [DimOverlay] 의 바깥 클릭이 실행되지 않도록 클릭을 소비 */
@Composable
fun Modifier.consumeClicks(): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = {},
)
