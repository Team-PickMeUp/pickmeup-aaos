package com.example.pickmeup.ui.terms

import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pickmeup.R
import com.example.pickmeup.ui.design.ProvideDesignScale
import com.example.pickmeup.ui.design.isCompactLayout
import com.example.pickmeup.ui.design.px
import com.example.pickmeup.ui.design.pxSp
import com.example.pickmeup.ui.theme.PickMeUpColors
import com.example.pickmeup.ui.theme.astaStyle

/** 약관 조항: 제목 + 본문 */
data class TermsArticle(@StringRes val title: Int, @StringRes val body: Int)

/** 위치기반서비스 이용약관 (Figma 2-2-1 예시 문구) */
private val LocationArticles = listOf(
    TermsArticle(R.string.terms_location_article1_title, R.string.terms_location_article1_body),
    TermsArticle(R.string.terms_location_article2_title, R.string.terms_location_article2_body),
    TermsArticle(R.string.terms_location_article3_title, R.string.terms_location_article3_body),
)

/** 개인정보 처리방침 (Figma 2-2-3 "원문 등록 예정" 안내) */
private val PrivacyArticles = listOf(
    TermsArticle(R.string.terms_privacy_article1_title, R.string.terms_privacy_article1_body),
    TermsArticle(R.string.terms_privacy_article2_title, R.string.terms_privacy_article2_body),
    TermsArticle(R.string.terms_privacy_article3_title, R.string.terms_privacy_article3_body),
)

/**
 * 동의 항목. 모두 필수입니다.
 * 화면 경로(TermsDetailDestination)의 인자로 쓰여서 release 빌드 난독화에서 제외(@Keep)합니다.
 */
@Keep
enum class TermsType(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @StringRes val intro: Int,
    val articles: List<TermsArticle>,
) {
    Service(
        R.string.terms_service_title,
        R.string.terms_service_desc,
        R.string.terms_service_intro,
        listOf(TermsArticle(R.string.terms_service_placeholder_title, R.string.terms_service_placeholder_body)),
    ),
    Privacy(
        R.string.terms_privacy_title,
        R.string.terms_privacy_desc,
        R.string.terms_privacy_intro,
        PrivacyArticles,
    ),
    Location(
        R.string.terms_location_title,
        R.string.terms_location_desc,
        R.string.terms_location_intro,
        LocationArticles,
    ),
}

/**
 * 최초 접속 시 이용약관 동의 화면 (Figma node 149:17350 "픽미업 최초 실행 동의 팝업").
 *
 * 상태바·클러스터(driving_view)·하단 GNB는 Pleos 시스템 UI이므로 여기서 그리지 않고,
 * 앱 창 영역만 그립니다.
 */
@Composable
fun TermsAgreementRoute(
    onAgreed: () -> Unit,
    onDisagree: () -> Unit,
    onViewDetail: (TermsType) -> Unit,
    viewModel: TermsAgreementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TermsAgreementScreen(
        uiState = uiState,
        onCheckedChange = viewModel::onCheckedChange,
        onCheckAllChange = viewModel::onCheckAllChange,
        onAgreeAndStart = { if (viewModel.agree()) onAgreed() },
        onDisagree = onDisagree,
        onViewDetail = onViewDetail,
    )
}

@Composable
fun TermsAgreementScreen(
    uiState: TermsAgreementUiState,
    onCheckedChange: (TermsType, Boolean) -> Unit,
    onCheckAllChange: (Boolean) -> Unit,
    onAgreeAndStart: () -> Unit,
    onDisagree: () -> Unit,
    onViewDetail: (TermsType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val allChecked = uiState.allChecked

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
                .verticalScroll(rememberScrollState())
                .padding(start = 64.px, end = 64.px, top = 48.px, bottom = 64.px),
        ) {
            // 상단 헤더
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(96.px),
                horizontalArrangement = Arrangement.spacedBy(24.px),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Figma "뒤로 가기" 슬롯은 비어 있음(최초 실행이라 돌아갈 곳이 없음). 자리만 유지.
                Spacer(Modifier.size(width = 50.px, height = 80.px))
                Text(
                    text = stringResource(R.string.terms_screen_title),
                    style = astaStyle(40.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 52f / 40f),
                )
            }

            // 동의 팝업 영역
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = contentInset, end = contentInset, top = 32.px),
                verticalArrangement = Arrangement.spacedBy(40.px),
            ) {
                // 팝업 헤더
                Column(verticalArrangement = Arrangement.spacedBy(16.px)) {
                    Text(
                        text = stringResource(R.string.terms_guide),
                        style = astaStyle(30.pxSp, FontWeight.Normal, PickMeUpColors.TextStrong, 38f / 30f),
                    )
                    Text(
                        text = stringResource(R.string.terms_guide_sub),
                        style = astaStyle(26.pxSp, FontWeight.Normal, PickMeUpColors.TextMuted, 34f / 26f),
                    )
                }

                // 동의 항목 리스트
                Column(verticalArrangement = Arrangement.spacedBy(16.px)) {
                    TermsType.entries.forEach { type ->
                        TermsItemCard(
                            type = type,
                            checked = type in uiState.checked,
                            onCheckedChange = { onCheckedChange(type, it) },
                            onViewDetail = { onViewDetail(type) },
                        )
                    }
                }

                // 선택 영역
                Column(verticalArrangement = Arrangement.spacedBy(16.px)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = allChecked,
                                role = Role.Checkbox,
                                onValueChange = onCheckAllChange,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(16.px),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CheckMark(checked = allChecked)
                        Text(
                            text = stringResource(R.string.terms_agree_all),
                            style = astaStyle(32.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 40f / 32f),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.px),
                    ) {
                        ActionButton(
                            text = stringResource(R.string.terms_agree_and_start),
                            enabled = allChecked,
                            primaryStyle = true,
                            onClick = onAgreeAndStart,
                            modifier = Modifier.weight(1f),
                        )
                        ActionButton(
                            text = stringResource(R.string.terms_disagree),
                            enabled = true,
                            primaryStyle = false,
                            onClick = onDisagree,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Text(
                        text = stringResource(R.string.terms_footnote),
                        style = astaStyle(24.pxSp, FontWeight.Normal, PickMeUpColors.TextMuted, 28f / 24f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TermsItemCard(
    type: TermsType,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onViewDetail: () -> Unit,
) {
    val shape = RoundedCornerShape(24.px)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PickMeUpColors.Surface)
            .border(1.px, PickMeUpColors.Border, shape)
            .padding(24.px),
        verticalArrangement = Arrangement.spacedBy(16.px),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
            horizontalArrangement = Arrangement.spacedBy(16.px),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CheckMark(checked = checked)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.px)) {
                Text(
                    text = stringResource(type.title),
                    style = astaStyle(32.pxSp, FontWeight.Bold, PickMeUpColors.TextPrimary, 40f / 32f),
                )
                Text(
                    text = stringResource(type.description),
                    style = astaStyle(26.pxSp, FontWeight.Normal, PickMeUpColors.TextStrong, 34f / 26f),
                )
            }
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(PickMeUpColors.Fill)
                    .padding(horizontal = 14.px, vertical = 8.px),
            ) {
                Text(
                    text = stringResource(R.string.terms_required),
                    style = astaStyle(20.pxSp, FontWeight.Normal, PickMeUpColors.TextMuted, 24f / 20f),
                )
            }
        }

        Row(
            Modifier.clickable(role = Role.Button, onClick = onViewDetail),
            horizontalArrangement = Arrangement.spacedBy(8.px),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.terms_view_detail),
                style = astaStyle(24.pxSp, FontWeight.Normal, PickMeUpColors.TextPrimary, 28f / 24f),
            )
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.px),
            )
        }
    }
}

/**
 * 32px 원형 체크 표시.
 * Figma에는 미체크 상태만 있어서, 체크 상태 색상(진한 배경 + 흰 체크)은 임의로 정했습니다.
 */
@Composable
private fun CheckMark(checked: Boolean) {
    Box(
        Modifier
            .size(32.px)
            .clip(RoundedCornerShape(16.px))
            .background(if (checked) PickMeUpColors.TextPrimary else PickMeUpColors.Fill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            tint = if (checked) Color.White else PickMeUpColors.TextDisabled,
            modifier = Modifier.size(18.px),
        )
    }
}

/**
 * 하단 88px 버튼.
 * - 활성 스타일(흰 배경/진한 글자) = Figma "동의하고 시작하기"
 * - 회색 스타일 = Figma "동의하지 않음"
 * 시작하기 버튼은 비활성일 때 회색 스타일로 표시합니다.
 */
@Composable
private fun ActionButton(
    text: String,
    enabled: Boolean,
    primaryStyle: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.px)
    val whiteStyle = primaryStyle && enabled
    Box(
        modifier
            .height(88.px)
            .clip(shape)
            .background(if (whiteStyle) PickMeUpColors.Surface else PickMeUpColors.Fill)
            .border(
                1.px,
                if (whiteStyle) PickMeUpColors.BorderStrong else PickMeUpColors.Border,
                shape,
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = astaStyle(
                32.pxSp,
                FontWeight.Bold,
                if (whiteStyle) PickMeUpColors.TextPrimary else PickMeUpColors.TextFaint,
                40f / 32f,
            ),
        )
    }
}

@Preview(widthDp = 1700, heightDp = 1280, showBackground = true)
@Composable
private fun TermsAgreementScreenPreview() {
    TermsAgreementScreen(
        uiState = TermsAgreementUiState(),
        onCheckedChange = { _, _ -> }, onCheckAllChange = {},
        onAgreeAndStart = {}, onDisagree = {}, onViewDetail = {},
    )
}
