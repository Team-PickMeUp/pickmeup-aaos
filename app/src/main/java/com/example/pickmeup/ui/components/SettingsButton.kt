package com.example.pickmeup.ui.components

import androidx.annotation.DrawableRes
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
 * 설정 버튼 (Figma: 설정아이콘 / Pleos 48px grid / settings / outlined, node 442:27123).
 */
@Composable
fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HeaderIconButton(
        icon = R.drawable.ic_settings,
        contentDescription = stringResource(R.string.main_settings),
        onClick = onClick,
        modifier = modifier,
    )
}

/**
 * 앱 사용 가이드 버튼 (Figma: book-icon / Pleos 48px grid / book-open / outlined, node 442:27121).
 * 설정 버튼 왼쪽에 놓입니다.
 */
@Composable
fun GuideButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HeaderIconButton(
        icon = R.drawable.ic_book_open,
        contentDescription = stringResource(R.string.main_app_guide),
        onClick = onClick,
        modifier = modifier,
    )
}

/** 화면 오른쪽 위 아이콘 버튼: 60px 터치 영역 안에 48px 아이콘 (Figma 6px 여백) */
@Composable
private fun HeaderIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(60.px),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = Color.Unspecified,
            modifier = Modifier.size(48.px),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsButtonPreview() {
    SettingsButton(onClick = {})
}

@Preview(showBackground = true)
@Composable
private fun GuideButtonPreview() {
    GuideButton(onClick = {})
}
