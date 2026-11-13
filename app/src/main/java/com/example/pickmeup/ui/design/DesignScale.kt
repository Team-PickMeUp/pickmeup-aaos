package com.example.pickmeup.ui.design

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Figma 와이어프레임은 2560×1440 px 차량 화면 기준이고, 앱 창(팝업)은 1700×1280 px 입니다.
 * 실제 기기의 화면 밀도(dpi)와 무관하게 Figma 비율 그대로 그리기 위해,
 * "Figma 1px = 몇 dp" 인지를 창 크기로 계산해서 [LocalPxScale]로 내려줍니다.
 */
const val DESIGN_WINDOW_WIDTH_PX = 1700f

val LocalPxScale = staticCompositionLocalOf { 1f }

/** Figma px → dp */
val Number.px: Dp
    @Composable @ReadOnlyComposable
    get() = (toFloat() * LocalPxScale.current).dp

/** Figma px → sp (시스템 글자 크기 설정은 그대로 반영됨) */
val Number.pxSp: TextUnit
    @Composable @ReadOnlyComposable
    get() = (toFloat() * LocalPxScale.current).sp

/**
 * 창 너비에 맞춰 Figma px 스케일을 계산해 하위 컴포저블에 제공합니다.
 *
 * 높이는 기준으로 쓰지 않습니다. 키보드(IME)가 올라오면 창 높이가 줄어드는데,
 * 높이까지 기준으로 삼으면 화면 전체가 갑자기 작아지기 때문입니다.
 * 세로로 넘치는 화면은 각 화면에서 스크롤로 처리합니다.
 */
@Composable
fun ProvideDesignScale(
    modifier: Modifier = Modifier,
    designWidthPx: Float = DESIGN_WINDOW_WIDTH_PX,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier) {
        val scale = (maxWidth.value / designWidthPx)
            .takeIf { it.isFinite() && it > 0f } ?: 1f
        CompositionLocalProvider(LocalPxScale provides scale, content = content)
    }
}
