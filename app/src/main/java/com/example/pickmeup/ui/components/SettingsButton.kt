package com.example.pickmeup.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pickmeup.R
import com.example.pickmeup.ui.design.px

/**
 * 설정 버튼 (Figma: 메인화면 / 설정아이콘, node 18:21228).
 * 60×60px 톱니바퀴 아이콘으로, 누르면 [onClick]이 호출됩니다.
 */
@Composable
fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(60.px),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_settings),
            contentDescription = stringResource(R.string.main_settings),
            tint = Color.Unspecified,
            modifier = Modifier.size(60.px),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsButtonPreview() {
    SettingsButton(onClick = {})
}
