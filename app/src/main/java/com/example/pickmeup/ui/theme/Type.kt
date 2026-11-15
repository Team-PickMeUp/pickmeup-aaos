package com.example.pickmeup.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.pickmeup.R

/** Figma 디자인 폰트: Asta Sans (OFL, 가변 폰트) */
@OptIn(ExperimentalTextApi::class)
val AstaSans = FontFamily(
    Font(
        R.font.asta_sans,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.asta_sans,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.asta_sans,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.asta_sans,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
    Font(
        R.font.asta_sans,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800)),
    ),
)

/**
 * Figma 텍스트 스타일 → TextStyle.
 * @param size `34.pxSp` 처럼 Figma 크기 그대로
 * @param lineHeightMultiplier Figma 행간 ÷ 글자 크기 (예: 44px / 34px). null 이면 폰트 기본값
 */
fun astaStyle(
    size: TextUnit,
    weight: FontWeight,
    color: Color,
    lineHeightMultiplier: Float? = null,
) = TextStyle(
    fontFamily = AstaSans,
    fontWeight = weight,
    fontSize = size,
    color = color,
    lineHeight = if (lineHeightMultiplier != null) size * lineHeightMultiplier else TextUnit.Unspecified,
    lineBreak = KoreanLineBreak,
)

/**
 * 한국어 문장이 단어 중간("주/세요")에서 끊기지 않도록 구문(어절) 단위로 줄바꿈 (Android 13+).
 * 좁은 창(화면 3분할)에서 긴 안내 문구가 여러 줄로 나뉠 때 필요합니다.
 */
private val KoreanLineBreak = LineBreak.Paragraph.copy(wordBreak = LineBreak.WordBreak.Phrase)

/** 스타일을 지정하지 않은 Material 텍스트의 기본값 */
internal val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = AstaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
