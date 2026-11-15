package com.example.pickmeup.ui.terms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.pickmeup.ui.components.ScreenHeader
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.isCompactLayout
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

/**
 * 약관 전문 화면.
 * Figma: "위치기반서비스 이용약관"(136:14057), "개인정보 처리방침"(136:15187).
 * 서비스 이용약관은 Figma에 화면이 없어 같은 레이아웃을 씁니다.
 * 동의 화면의 "내용 보기"와 설정 화면의 약관 메뉴에서 함께 사용합니다.
 */
@Composable
fun TermsDetailScreen(
    type: TermsType,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProvideDesignScale(
        modifier
            .fillMaxSize()
            .background(PickMeUpColors.BackgroundPrimary),
    ) {
        // 본문 안쪽 여백: 좁은 창(화면 3분할)에서는 76 → 16px
        val contentInset = if (isCompactLayout) 16.px else 76.px
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 64.px, end = 64.px, top = 48.px),
        ) {
            ScreenHeader(title = stringResource(type.title), onBack = onBack)

            // 약관 상세 영역 (내용이 길면 스크롤)
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = contentInset, end = contentInset, top = 32.px, bottom = 64.px),
                verticalArrangement = Arrangement.spacedBy(40.px),
            ) {
                Text(
                    text = stringResource(type.intro),
                    style = astaStyle(32.pxSp, FontWeight.Normal, PickMeUpColors.TextStrong, 40f / 32f),
                )
                Column(verticalArrangement = Arrangement.spacedBy(24.px)) {
                    type.articles.forEach { article ->
                        Text(
                            text = stringResource(article.title),
                            style = astaStyle(32.pxSp, FontWeight.SemiBold, PickMeUpColors.TextPrimary, 40f / 32f),
                        )
                        Text(
                            text = stringResource(article.body),
                            style = astaStyle(26.pxSp, FontWeight.Normal, PickMeUpColors.TextStrong, 34f / 26f),
                        )
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 1700, heightDp = 1184, showBackground = true)
@Composable
private fun TermsDetailScreenPreview() {
    TermsDetailScreen(type = TermsType.Location, onBack = {})
}
