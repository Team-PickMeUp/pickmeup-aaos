package com.example.pickmeup.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.example.pickmeup.R
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.terms.astaStyle
import com.example.pickmeup.ui.theme.PickMeUpColors

/**
 * 96px 높이 헤더: 80px 뒤로 가기 버튼(48px 왼쪽 화살표) + 52px 굵은 제목.
 * Figma: 설정 헤더(44:8911), 약관 상세 상단 헤더(148:14601)
 */
@Composable
fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(96.px),
        horizontalArrangement = Arrangement.spacedBy(24.px),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(80.px)
                .clickable(role = Role.Button, onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_left),
                contentDescription = stringResource(R.string.common_back),
                tint = Color.Unspecified,
                modifier = Modifier.size(48.px),
            )
        }
        Text(
            text = title,
            style = astaStyle(52.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary),
        )
    }
}
