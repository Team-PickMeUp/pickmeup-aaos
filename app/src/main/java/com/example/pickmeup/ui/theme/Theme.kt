package com.example.pickmeup.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * 앱 테마. 화면 색상은 [PickMeUpColors] 를 직접 쓰고,
 * 여기서는 Material 컴포넌트 기본값(라이트 테마, Asta Sans)만 정합니다.
 * Figma 에 다크 모드 디자인이 없어서 라이트 테마만 지원합니다.
 */
@Composable
fun PickMeUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        typography = Typography,
        content = content,
    )
}
