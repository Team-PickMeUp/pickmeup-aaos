package com.example.pickmeup.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.example.pickmeup.R
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

/**
 * 글자 화살표 뒤로가기 "←  문구" (Figma: 픽업 불러오기 / 저장된 픽업 대상자 목록 상단).
 * 화면마다 글자 크기가 달라서 크기·행간을 받고, [contentPadding] 은 터치 영역 안쪽 여백입니다.
 */
@Composable
fun TextBackButton(
    text: String,
    fontSize: TextUnit,
    lineHeightMultiplier: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Text(
        text = "←  $text",
        style = astaStyle(fontSize, FontWeight.Bold, PickMeUpColors.TextPrimary, lineHeightMultiplier),
        modifier = modifier
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.common_back),
                onClick = onClick,
            )
            .padding(contentPadding),
    )
}
