package com.example.pickmeup.ui.design

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Figma 와이어프레임은 2560×1440 px 차량 화면(Connect-S 17") 기준입니다.
 * Pleos Connect display guide 2.1.5 의 앱 영역 (dp):
 *
 * | 폼팩터               | Landscape (1:2) | Portrait (1:1:1, 화면 3분할) |
 * |----------------------|-----------------|------------------------------|
 * | Connect-S-17 (기본)  | 1700 × 1184     | 840 × 1184                   |
 * | Connect-S-15.6       | 1408 × 1018     | 692 × 1018                   |
 * | Connect-S-14.6       | 1360 × 948      | 672 × 948                    |
 * | Connect-L-12.9       | 1274 × 855      | 628 × 855                    |
 * | Connect-W-Standard   | 1640 × 810      | 800 × 810                    |
 * | Connect-W-Extended   | 1640 × 1400     | 800 × 1400                   |
 * | Connect-WF           | 1256 × 800      | 648 × 800                    |
 *
 * 기기 밀도(dpi)와 무관하게 Figma 비율 그대로 그리기 위해 "Figma 1px = 몇 dp" 를 창 크기로 계산해
 * [LocalPxScale] 로 내려주고, Portrait(좁은 창)이면 [LocalCompactLayout] 을 켭니다.
 */

/** Landscape 앱 영역 기준 크기 (Connect-S-17) */
const val DESIGN_WINDOW_WIDTH_PX = 1700f
const val DESIGN_WINDOW_HEIGHT_PX = 1184f

/** Portrait(화면 3분할) 앱 영역 기준 너비 (Connect-S-17) */
const val COMPACT_DESIGN_WIDTH_PX = 840f

/** 이 너비보다 좁은 창은 Portrait 로 봄 (가이드 기준 Portrait 최대 840dp, Landscape 최소 1256dp) */
val COMPACT_WINDOW_MAX_WIDTH = 1000.dp

/** Pleos 타이포그래피 가이드: 가독성을 위해 글자는 최소 18px */
private const val MIN_TEXT_PX = 18f

val LocalPxScale = staticCompositionLocalOf { 1f }

/** 좁은 창(Portrait, 화면 3분할) 여부. 가로 배치 → 세로 배치, 고정 폭 → 창 폭 맞춤으로 바꿀 때 씁니다. */
val LocalCompactLayout = staticCompositionLocalOf { false }

/** 좁은 창(Portrait, 화면 3분할)이면 true */
val isCompactLayout: Boolean
    @Composable @ReadOnlyComposable
    get() = LocalCompactLayout.current

/** Figma px → dp */
val Number.px: Dp
    @Composable @ReadOnlyComposable
    get() = (toFloat() * LocalPxScale.current).dp

/**
 * Figma px → sp (시스템 글자 크기 설정은 그대로 반영됨).
 * 작은 폼팩터에서 줄어들어도 18px 이상이던 글자는 18 아래로 내려가지 않습니다 (Pleos 최소 글자 크기).
 */
val Number.pxSp: TextUnit
    @Composable @ReadOnlyComposable
    get() {
        val design = toFloat()
        val scaled = design * LocalPxScale.current
        return (if (design >= MIN_TEXT_PX) maxOf(scaled, MIN_TEXT_PX) else scaled).sp
    }

/**
 * 창 크기에 맞춰 Figma px 스케일을 계산해 하위 컴포저블에 제공합니다.
 *
 * - 너비: Landscape 는 1700px, Portrait(창 너비 < [COMPACT_WINDOW_MAX_WIDTH]) 는 840px 을 창 너비로 봄
 * - 높이: 창 높이가 1184px 보다 낮은 폼팩터(Connect-W 등)에서도 넘치지 않도록 높이 비율도 함께 봄
 *
 * 높이는 키보드(IME)에 따라 바뀌는 레이아웃 높이 대신 창 높이([LocalWindowInfo])를 씁니다.
 * 키보드가 올라올 때 화면 전체가 갑자기 작아지지 않게 하기 위해서입니다.
 * 그래도 세로로 넘치는 화면은 각 화면에서 스크롤로 처리합니다.
 */
@Composable
fun ProvideDesignScale(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val windowHeightDp = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp().value }
    BoxWithConstraints(modifier) {
        val compact = maxWidth < COMPACT_WINDOW_MAX_WIDTH
        val widthScale = maxWidth.value / if (compact) COMPACT_DESIGN_WIDTH_PX else DESIGN_WINDOW_WIDTH_PX
        val heightScale = windowHeightDp / DESIGN_WINDOW_HEIGHT_PX
        val scale = (if (heightScale > 0f) minOf(widthScale, heightScale) else widthScale)
            .takeIf { it.isFinite() && it > 0f } ?: 1f
        CompositionLocalProvider(
            LocalPxScale provides scale,
            LocalCompactLayout provides compact,
            content = content,
        )
    }
}

/**
 * 팝업 너비: Figma 너비 그대로 쓰되, 창이 좁으면(화면 3분할) 양옆 40px 여백을 남기고 줄어듭니다.
 * 팝업 안 요소는 고정 너비 대신 fillMaxWidth / weight 로 맞춰 주세요.
 */
@Composable
fun Modifier.popupWidth(designWidthPx: Number): Modifier =
    padding(horizontal = 40.px).widthIn(max = designWidthPx.px).fillMaxWidth()
